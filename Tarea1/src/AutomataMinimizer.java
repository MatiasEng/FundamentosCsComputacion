import java.util.*;

public class AutomataMinimizer {

    public static Automata minimize(Automata dfa) {
        if (!dfa.isDFA()) {
            throw new IllegalArgumentException("El autómata debe ser un AFD para minimizarlo.");
        }

        Set<String> reachableStates = getReachableStates(dfa);

        Set<String> finalStates = new HashSet<>();
        Set<String> nonFinalStates = new HashSet<>();

        for (String state : reachableStates) {
            if (dfa.getFinalStates().contains(state)) {
                finalStates.add(state);
            } else {
                nonFinalStates.add(state);
            }
        }

        List<Set<String>> partitions = new ArrayList<>();
        if (!finalStates.isEmpty()) partitions.add(finalStates);
        if (!nonFinalStates.isEmpty()) partitions.add(nonFinalStates);

        boolean changed = true;
        while (changed) {
            changed = false;
            List<Set<String>> newPartitions = new ArrayList<>();

            for (Set<String> group : partitions) {
                Map<String, Set<String>> splitMap = new HashMap<>();

                for (String state : group) {
                    StringBuilder signature = new StringBuilder();
                    for (String symbol : dfa.getAlphabet()) {
                        String target = getTargetState(dfa, state, symbol);
                        int targetGroupIndex = findGroupIndex(partitions, target);
                        signature.append(symbol).append("->").append(targetGroupIndex).append(";");
                    }

                    splitMap.putIfAbsent(signature.toString(), new HashSet<>());
                    splitMap.get(signature.toString()).add(state);
                }

                newPartitions.addAll(splitMap.values());
                if (splitMap.size() > 1) {
                    changed = true;
                }
            }
            partitions = newPartitions;
        }

        return buildMinimizedDFA(dfa, partitions, reachableStates);
    }

    private static Set<String> getReachableStates(Automata dfa) {
        Set<String> reachable = new HashSet<>();
        Queue<String> queue = new LinkedList<>();

        queue.add(dfa.getInitialState());
        reachable.add(dfa.getInitialState());

        while (!queue.isEmpty()) {
            String state = queue.poll();
            Map<String, Set<String>> trans = dfa.getTransitions().get(state);

            if (trans != null) {
                for (String symbol : dfa.getAlphabet()) {
                    Set<String> targets = trans.get(symbol);
                    if (targets != null) {
                        for (String target : targets) {
                            if (!reachable.contains(target)) {
                                reachable.add(target);
                                queue.add(target);
                            }
                        }
                    }
                }
            }
        }
        return reachable;
    }

    private static String getTargetState(Automata dfa, String state, String symbol) {
        Map<String, Set<String>> trans = dfa.getTransitions().get(state);
        if (trans != null && trans.containsKey(symbol)) {
            return trans.get(symbol).iterator().next();
        }
        return null;
    }

    private static int findGroupIndex(List<Set<String>> partitions, String state) {
        if (state == null) return -1;
        for (int i = 0; i < partitions.size(); i++) {
            if (partitions.get(i).contains(state)) {
                return i;
            }
        }
        return -1;
    }

    private static Automata buildMinimizedDFA(Automata originalDfa, List<Set<String>> partitions, Set<String> reachable) {
        Automata minDfa = new Automata();
        minDfa.setAlphabet(new HashSet<>(originalDfa.getAlphabet()));
        minDfa.setDFA(true);

        Map<String, String> stateToGroup = new HashMap<>();
        for (Set<String> group : partitions) {
            String groupName = "M{" + String.join(",", group) + "}";
            minDfa.getStates().add(groupName);
            for (String state : group) {
                stateToGroup.put(state, groupName);

                if (state.equals(originalDfa.getInitialState())) {
                    minDfa.setInitialState(groupName);
                }
                if (originalDfa.getFinalStates().contains(state)) {
                    minDfa.getFinalStates().add(groupName);
                }
            }
        }

        for (String state : reachable) {
            String fromGroup = stateToGroup.get(state);
            Map<String, Set<String>> trans = originalDfa.getTransitions().get(state);

            if (trans != null) {
                for (String symbol : originalDfa.getAlphabet()) {
                    Set<String> targets = trans.get(symbol);
                    if (targets != null && !targets.isEmpty()) {
                        String target = targets.iterator().next();
                        String toGroup = stateToGroup.get(target);
                        minDfa.addTransition(fromGroup, symbol, toGroup);
                    }
                }
            }
        }

        return minDfa;
    }
}
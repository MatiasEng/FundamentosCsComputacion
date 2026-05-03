import java.util.*;

public class AutomataEquivalenceChecker {

    public static boolean areEquivalent(Automata min1, Automata min2) {
        if (!min1.getAlphabet().equals(min2.getAlphabet())) {
            return false;
        }

        Queue<StatePair> queue = new LinkedList<>();
        Set<StatePair> visited = new HashSet<>();

        StatePair initialPair = new StatePair(min1.getInitialState(), min2.getInitialState());
        queue.add(initialPair);
        visited.add(initialPair);

        while (!queue.isEmpty()) {
            StatePair current = queue.poll();

            boolean isFinal1 = min1.getFinalStates().contains(current.state1);
            boolean isFinal2 = min2.getFinalStates().contains(current.state2);


            if (isFinal1 != isFinal2) {
                return false;
            }

            for (String symbol : min1.getAlphabet()) {
                String next1 = getNextState(min1, current.state1, symbol);
                String next2 = getNextState(min2, current.state2, symbol);

                if ((next1 == null && next2 != null) || (next1 != null && next2 == null)) {
                    return false;
                }

                if (next1 != null && next2 != null) {
                    StatePair nextPair = new StatePair(next1, next2);
                    if (!visited.contains(nextPair)) {
                        visited.add(nextPair);
                        queue.add(nextPair);
                    }
                }
            }
        }

        return true;
    }

    private static String getNextState(Automata dfa, String state, String symbol) {
        Map<String, Set<String>> trans = dfa.getTransitions().get(state);
        if (trans != null && trans.containsKey(symbol)) {
            Set<String> targets = trans.get(symbol);
            if (targets != null && !targets.isEmpty()) {
                return targets.iterator().next();
            }
        }
        return null;
    }

    private static class StatePair {
        String state1;
        String state2;

        public StatePair(String state1, String state2) {
            this.state1 = state1;
            this.state2 = state2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StatePair statePair = (StatePair) o;
            return Objects.equals(state1, statePair.state1) &&
                    Objects.equals(state2, statePair.state2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(state1, state2);
        }
    }
}
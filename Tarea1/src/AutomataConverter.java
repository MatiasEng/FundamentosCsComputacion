import java.util.*;

/**
 * Converts NFA to DFA using subset construction algorithm.
 */
public class AutomataConverter {

    /**
     * Converts an NFA to an equivalent DFA.
     */
    public static Automata toDFA(Automata nfa) {
        if (nfa.isDFA()) {
            return nfa; // Already a DFA
        }

        Automata dfa = new Automata();
        dfa.setAlphabet(new HashSet<>(nfa.getAlphabet()));

        // Subset construction
        Map<Set<String>, String> subsetToStateName = new HashMap<>();
        Map<String, Set<String>> stateNameToSubset = new HashMap<>();
        List<Set<String>> unprocessedSubsets = new ArrayList<>();

        // Start with epsilon-closure of initial state
        Set<String> initialStateSet = epsilonClosure(nfa, Set.of(nfa.getInitialState()));
        String initialStateName = getStateName(initialStateSet);
        subsetToStateName.put(initialStateSet, initialStateName);
        stateNameToSubset.put(initialStateName, initialStateSet);
        unprocessedSubsets.add(initialStateSet);
        dfa.setInitialState(initialStateName);

        // Process subsets
        Map<String, Map<String, String>> dfaTransitions = new HashMap<>();

        while (!unprocessedSubsets.isEmpty()) {
            Set<String> subset = unprocessedSubsets.remove(0);
            String fromState = subsetToStateName.get(subset);

            dfaTransitions.putIfAbsent(fromState, new HashMap<>());

            // For each symbol in alphabet
            for (String symbol : nfa.getAlphabet()) {
                // Compute move(subset, symbol)
                Set<String> moveSet = new HashSet<>();
                for (String state : subset) {
                    Map<String, Set<String>> stateTrans = nfa.getTransitions().get(state);
                    if (stateTrans != null && stateTrans.containsKey(symbol)) {
                        moveSet.addAll(stateTrans.get(symbol));
                    }
                }

                // Compute epsilon-closure of moveSet
                Set<String> closureSet = epsilonClosure(nfa, moveSet);

                if (!closureSet.isEmpty()) {
                    // Get or create state name for this subset
                    String toStateName;
                    if (subsetToStateName.containsKey(closureSet)) {
                        toStateName = subsetToStateName.get(closureSet);
                    } else {
                        toStateName = getStateName(closureSet);
                        subsetToStateName.put(closureSet, toStateName);
                        stateNameToSubset.put(toStateName, closureSet);
                        unprocessedSubsets.add(closureSet);
                    }

                    dfaTransitions.get(fromState).put(symbol, toStateName);
                }
            }
        }

        // Build DFA transitions
        dfa.setStates(new HashSet<>(dfaTransitions.keySet()));
        for (String fromState : dfaTransitions.keySet()) {
            for (String symbol : dfaTransitions.get(fromState).keySet()) {
                String toState = dfaTransitions.get(fromState).get(symbol);
                dfa.addTransition(fromState, symbol, toState);
            }
        }

        // Set final states (any subset containing an original final state)
        Set<String> dfaFinalStates = new HashSet<>();
        for (Map.Entry<Set<String>, String> entry : subsetToStateName.entrySet()) {
            Set<String> subset = entry.getKey();
            String stateName = entry.getValue();

            for (String finalState : nfa.getFinalStates()) {
                if (subset.contains(finalState)) {
                    dfaFinalStates.add(stateName);
                    break;
                }
            }
        }
        dfa.setFinalStates(dfaFinalStates);
        dfa.setDFA(true);

        return dfa;
    }

    /**
     * Computes epsilon-closure of a set of states.
     */
    private static Set<String> epsilonClosure(Automata nfa, Set<String> states) {
        Set<String> closure = new HashSet<>(states);
        Stack<String> stack = new Stack<>();
        stack.addAll(states);

        while (!stack.isEmpty()) {
            String state = stack.pop();
            Map<String, Set<String>> stateTrans = nfa.getTransitions().get(state);

            if (stateTrans != null && stateTrans.containsKey("ε")) {
                for (String nextState : stateTrans.get("ε")) {
                    if (!closure.contains(nextState)) {
                        closure.add(nextState);
                        stack.push(nextState);
                    }
                }
            }
        }

        return closure;
    }

    /**
     * Generates a unique name for a set of states.
     */
    private static String getStateName(Set<String> states) {
        List<String> sortedStates = new ArrayList<>(states);
        Collections.sort(sortedStates);
        return "{" + String.join(",", sortedStates) + "}";
    }
}
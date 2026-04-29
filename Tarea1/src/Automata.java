import java.io.*;
import java.util.*;

/**
 * Represents a Finite Automaton (NFA or DFA).
 */
public class Automata {
    private Set<String> states;
    private Set<String> alphabet;
    private Map<String, Map<String, Set<String>>> transitions; // For NFA
    private String initialState;
    private Set<String> finalStates;
    private boolean isDFA;

    public Automata() {
        this.states = new HashSet<>();
        this.alphabet = new HashSet<>();
        this.transitions = new HashMap<>();
        this.finalStates = new HashSet<>();
        this.isDFA = true; // Assume DFA until proven otherwise
    }

    /**
     * Reads an automaton from a file following the specified format.
     * Format:
     * Number of states: N
     * States: q0 q1 q2 ... qN-1
     * Alphabet: a b c ...
     * Initial state: q0
     * Final states: q2 q3
     * Transitions:
     * q0 a q1
     * q0 b q2
     * ...
     */
    public static Automata readFromFile(String filename) throws IOException {
        Automata aut = new Automata();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("Number of states:")) {
                // Skip - we'll get states from next line
                continue;
            } else if (line.startsWith("States:")) {
                String[] states = line.substring(7).trim().split("\\s+");
                aut.states.addAll(Arrays.asList(states));
            } else if (line.startsWith("Alphabet:")) {
                String[] symbols = line.substring(8).trim().split("\\s+");
                aut.alphabet.addAll(Arrays.asList(symbols));
            } else if (line.startsWith("Initial state:")) {
                aut.initialState = line.substring(14).trim();
            } else if (line.startsWith("Final states:")) {
                String[] finals = line.substring(12).trim().split("\\s+");
                aut.finalStates.addAll(Arrays.asList(finals));
            } else if (line.startsWith("Transitions:")) {
                // Read transitions until EOF
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] parts = line.split("\\s+");
                    if (parts.length == 3) {
                        String from = parts[0];
                        String symbol = parts[1];
                        String to = parts[2];

                        aut.addTransition(from, symbol, to);
                    }
                }
            }
        }

        reader.close();
        aut.determineType();
        return aut;
    }

    /**
     * Adds a transition to the automaton.
     */
    public void addTransition(String from, String symbol, String to) {
        transitions.putIfAbsent(from, new HashMap<>());
        transitions.get(from).putIfAbsent(symbol, new HashSet<>());
        transitions.get(from).get(symbol).add(to);
    }

    /**
     * Determines if the automaton is DFA or NFA.
     */
    private void determineType() {
        // Check if any state has multiple transitions for the same symbol
        for (String state : transitions.keySet()) {
            Map<String, Set<String>> stateTransitions = transitions.get(state);
            for (String symbol : stateTransitions.keySet()) {
                if (stateTransitions.get(symbol).size() > 1) {
                    isDFA = false;
                    return;
                }
            }
        }

        // Check if all alphabet symbols have transitions from each state
        for (String state : states) {
            Map<String, Set<String>> stateTrans = transitions.getOrDefault(state, new HashMap<>());
            for (String symbol : alphabet) {
                if (!stateTrans.containsKey(symbol)) {
                    isDFA = false;
                    return;
                }
            }
        }
    }

    public String getType() {
        return isDFA ? "DFA" : "NFA";
    }

    public boolean isDFA() {
        return isDFA;
    }

    // Getters and setters
    public Set<String> getStates() { return states; }
    public Set<String> getAlphabet() { return alphabet; }
    public Map<String, Map<String, Set<String>>> getTransitions() { return transitions; }
    public String getInitialState() { return initialState; }
    public Set<String> getFinalStates() { return finalStates; }

    public void setStates(Set<String> states) { this.states = states; }
    public void setAlphabet(Set<String> alphabet) { this.alphabet = alphabet; }
    public void setTransitions(Map<String, Map<String, Set<String>>> transitions) {
        this.transitions = transitions;
    }
    public void setInitialState(String initialState) { this.initialState = initialState; }
    public void setFinalStates(Set<String> finalStates) { this.finalStates = finalStates; }
    public void setDFA(boolean isDFA) { this.isDFA = isDFA; }
}

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
        boolean inDelta = false;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("k={")) {
                // Extrae los estados
                String content = line.substring(3, line.length() - 1);
                aut.states.addAll(Arrays.asList(content.split(",")));
            } else if (line.startsWith("sigma={")) {
                // Extrae el alfabeto
                String content = line.substring(7, line.length() - 1);
                aut.alphabet.addAll(Arrays.asList(content.split(",")));
            } else if (line.equals("delta:")) {
                inDelta = true; // Comienza a leer transiciones
            } else if (line.startsWith("(") && inDelta) {
                // Lee las transiciones con formato (q0,a,q1)
                String content = line.substring(1, line.length() - 1);
                String[] parts = content.split(",");
                if (parts.length == 3) {
                    aut.addTransition(parts[0].trim(), parts[1].trim(), parts[2].trim());
                }
            } else if (line.startsWith("s=")) {
                inDelta = false;
                // Extrae el estado inicial
                aut.initialState = line.substring(2).trim();
            } else if (line.startsWith("f={")) {
                // Extrae los estados finales
                String content = line.substring(3, line.length() - 1);
                if (!content.isEmpty()) {
                    aut.finalStates.addAll(Arrays.asList(content.split(",")));
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

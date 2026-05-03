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
        this.isDFA = true;
    }

    /**
     * Reads an automaton from a file with format:
     * K={q0,q1,q2}
     * Sigma={a,b}
     * delta:
     * (q0,a,q0)
     * (q0,b,q1)
     * ...
     * s=q0
     * F={q1}
     */
    public static Automata readFromFile(String filename) throws IOException {
        Automata aut = new Automata();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        boolean readingDelta = false;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("K=")) {
                // Parse states: K={q0,q1,q2}
                String statesStr = line.substring(2); // Remove "K="
                statesStr = statesStr.substring(1, statesStr.length() - 1); // Remove { }
                String[] stateArray = statesStr.split(",");
                for (String state : stateArray) {
                    aut.states.add(state.trim());
                }
            }
            else if (line.startsWith("Sigma=")) {
                // Parse alphabet: Sigma={a,b}
                String sigmaStr = line.substring(6); // Remove "Sigma="
                sigmaStr = sigmaStr.substring(1, sigmaStr.length() - 1); // Remove { }
                String[] sigmaArray = sigmaStr.split(",");
                for (String symbol : sigmaArray) {
                    aut.alphabet.add(symbol.trim());
                }
            }
            else if (line.startsWith("delta:")) {
                readingDelta = true;
            }
            else if (readingDelta && line.startsWith("(")) {
                // Parse transition: (q0,a,q0)
                line = line.substring(1, line.length() - 1); // Remove ( )
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String from = parts[0].trim();
                    String symbol = parts[1].trim();
                    String to = parts[2].trim();
                    aut.addTransition(from, symbol, to);
                }
            }
            else if (line.startsWith("s=")) {
                // Parse initial state: s=q0
                aut.initialState = line.substring(2).trim();
            }
            else if (line.startsWith("F=")) {
                // Parse final states: F={q1} or F={q1,q2}
                String finalsStr = line.substring(2); // Remove "F="
                finalsStr = finalsStr.substring(1, finalsStr.length() - 1); // Remove { }
                if (!finalsStr.isEmpty()) {
                    String[] finalArray = finalsStr.split(",");
                    for (String finalState : finalArray) {
                        aut.finalStates.add(finalState.trim());
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
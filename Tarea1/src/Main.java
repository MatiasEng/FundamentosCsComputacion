import java.io.*;
import java.util.*;

/**
 * Main class for the Finite Automata project.
 * Implements NFA to DFA conversion, minimization, equivalence checking, and visualization.
 *
 * @authors [Your Names Here]
 * @version 1.0
 */
public class Main {
    public static void main(String[] args) {
        // Check command line arguments
        if (args.length != 2) {
            System.err.println("Usage: java Main <automaton1.txt> <automaton2.txt>");
            System.exit(1);
        }

        try {
            // Step 1: Read both automata from files
            System.out.println("=== Step 1: Reading Automata ===");
            Automata aut1 = Automata.readFromFile(args[0]);
            Automata aut2 = Automata.readFromFile(args[1]);

            System.out.println("Automaton 1: " + aut1.getType());
            System.out.println("Automaton 2: " + aut2.getType());

            // Step 2: Convert to DFA if necessary
            System.out.println("\n=== Step 2: Converting to DFA ===");
            Automata dfa1 = AutomataConverter.toDFA(aut1);
            Automata dfa2 = AutomataConverter.toDFA(aut2);

            System.out.println("After conversion:");
            System.out.println("Automaton 1: " + dfa1.getType());
            System.out.println("Automaton 2: " + dfa2.getType());

            // Step 3: Minimize both automata and check equivalence
            System.out.println("\n=== Step 3: Minimizing and Checking Equivalence ===");
            Automata min1 = AutomataMinimizer.minimize(dfa1);
            Automata min2 = AutomataMinimizer.minimize(dfa2);

            boolean equivalent = AutomataEquivalenceChecker.areEquivalent(min1, min2);
            System.out.println("The automata are " + (equivalent ? "EQUIVALENT" : "NOT EQUIVALENT"));

            // Step 4: Draw both automata
            System.out.println("\n=== Step 4: Generating Visualizations ===");
            GraphVizGenerator.generateGraph(min1, "automaton1_minimized");
            GraphVizGenerator.generateGraph(min2, "automaton2_minimized");

            System.out.println("\n=== Process Complete ===");
            System.out.println("Check the generated .png files for visualizations.");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
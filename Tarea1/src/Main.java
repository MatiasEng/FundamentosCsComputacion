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
        try {
            // Archivos de entrada fijos
            String file1 = "input/automaton1.txt";
            String file2 = "input/automaton2.txt";

            // Verificar que los archivos existan
            System.out.println("=== Verificando archivos de entrada ===");
            File f1 = new File(file1);
            File f2 = new File(file2);

            if (!f1.exists()) {
                System.err.println("Error: No se encuentra el archivo " + file1);
                System.err.println("Asegurate de crear la carpeta 'input' y colocar los archivos .txt");
                System.exit(1);
            }

            if (!f2.exists()) {
                System.err.println("Error: No se encuentra el archivo " + file2);
                System.err.println("Asegurate de crear la carpeta 'input' y colocar los archivos .txt");
                System.exit(1);
            }

            System.out.println("Archivo 1: " + file1 + " - OK");
            System.out.println("Archivo 2: " + file2 + " - OK");

            // Step 1: Read both automata from files
            System.out.println("\n=== Step 1: Reading Automata ===");
            Automata aut1 = Automata.readFromFile(file1);
            Automata aut2 = Automata.readFromFile(file2);

            System.out.println("Automaton 1: " + aut1.getType());
            System.out.println("Automaton 2: " + aut2.getType());

            // Mostrar información de los autómatas
            System.out.println("\n--- Automaton 1 Details ---");
            System.out.println("States: " + aut1.getStates());
            System.out.println("Alphabet: " + aut1.getAlphabet());
            System.out.println("Initial: " + aut1.getInitialState());
            System.out.println("Final: " + aut1.getFinalStates());

            System.out.println("\n--- Automaton 2 Details ---");
            System.out.println("States: " + aut2.getStates());
            System.out.println("Alphabet: " + aut2.getAlphabet());
            System.out.println("Initial: " + aut2.getInitialState());
            System.out.println("Final: " + aut2.getFinalStates());

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
            System.out.println("\n==========================================");
            System.out.println("RESULT: The automata are " + (equivalent ? "EQUIVALENT" : "NOT EQUIVALENT"));
            System.out.println("==========================================\n");

            // Step 4: Draw both automata
            System.out.println("=== Step 4: Generating Visualizations ===");
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
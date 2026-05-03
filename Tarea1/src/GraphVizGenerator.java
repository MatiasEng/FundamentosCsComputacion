import java.io.*;

public class GraphVizGenerator {
    public static void generateGraph(Automata aut, String filename) {
        try {
            File dotFile = new File(filename + ".dot");
            BufferedWriter writer = new BufferedWriter(new FileWriter(dotFile));
            writer.write("digraph Automata {\n");
            writer.write("  rankdir=LR;\n");
            writer.write("  \"\" [shape=none];\n"); // Nodo invisible para la flecha inicial

            for (String fState : aut.getFinalStates()) {
                writer.write("  \"" + fState + "\" [shape=doublecircle];\n");
            }
            writer.write("  node [shape=circle];\n");

            writer.write("  \"\" -> \"" + aut.getInitialState() + "\";\n");

            for (String from : aut.getTransitions().keySet()) {
                for (String symbol : aut.getTransitions().get(from).keySet()) {
                    for (String to : aut.getTransitions().get(from).get(symbol)) {
                        writer.write("  \"" + from + "\" -> \"" + to + "\" [label=\"" + symbol + "\"];\n");
                    }
                }
            }
            writer.write("}\n");
            writer.close();

            Process p = Runtime.getRuntime().exec("dot -Tpng " + filename + ".dot -o " + filename + ".png");
            p.waitFor();
            System.out.println("Imagen generada: " + filename + ".png");

        } catch (Exception e) {
            System.err.println("Error generando el gráfico: Asegúrate de tener Graphviz instalado.");
        }
    }
}
import java.io.*;      // Importa clases para manejo de archivos (File, BufferedWriter, FileWriter, etc.)

/**
 * Clase que genera representaciones visuales de autómatas usando GraphViz.
 *
 * GraphViz es una herramienta externa que convierte descripciones en formato DOT
 * a imágenes (PNG, PDF, SVG, etc.).
 *
 * El proceso consta de dos pasos:
 * 1. Generar un archivo .dot con la descripción del autómata en lenguaje DOT
 * 2. Ejecutar el comando "dot" de GraphViz para convertir el .dot a .png
 *
 * Para que esta clase funcione correctamente, GraphViz debe estar instalado
 * y accesible desde la línea de comandos (en el PATH del sistema).
 *
 * @authors [Tus Nombres Aquí]
 * @version 1.0
 */
public class GraphVizGenerator {

    /**
     * Genera un diagrama visual del autómata usando GraphViz.
     *
     * El método crea dos archivos:
     * - <filename>.dot: Descripción del autómata en formato DOT
     * - <filename>.png: Imagen generada por GraphViz
     *
     * @param aut El autómata a visualizar (puede ser AFD o AFND)
     * @param filename Nombre base para los archivos de salida (sin extensión)
     *
     * @example
     * GraphVizGenerator.generateGraph(automataMinimizado, "automaton1_minimized");
     * // Genera: automaton1_minimized.dot y automaton1_minimized.png
     */
    public static void generateGraph(Automata aut, String filename) {
        try {
            // ================================================================
            // PASO 1: Crear y escribir el archivo .dot
            // ================================================================
            // El archivo .dot contiene la descripción del grafo en lenguaje DOT
            // Formato DOT: https://graphviz.org/doc/info/lang.html

            File archivoDot = new File(filename + ".dot");
            BufferedWriter escritor = new BufferedWriter(new FileWriter(archivoDot));

            // ================================================================
            // CABECERA DEL GRAFO
            // ================================================================
            // "digraph" define un grafo dirigido (las flechas tienen dirección)
            escritor.write("digraph Automata {\n");

            // "rankdir=LR" hace que el grafo se dibuje de izquierda a derecha
            // (LR = Left to Right), lo que es estándar para autómatas
            escritor.write("  rankdir=LR;\n");

            // Nodo invisible (sin forma) para la flecha que apunta al estado inicial
            // Esto crea una flecha que no viene de ningún nodo
            escritor.write("  \"\" [shape=none];\n");

            // ================================================================
            // DEFINIR FORMA DE LOS ESTADOS FINALES
            // ================================================================
            // Los estados finales se dibujan como doble círculo (doublecircle)
            // Primero se definen todos para que la configuración se aplique correctamente
            for (String estadoFinal : aut.getEstadosFinales()) {
                escritor.write("  \"" + estadoFinal + "\" [shape=doublecircle];\n");
            }

            // ================================================================
            // DEFINIR FORMA DE LOS ESTADOS NO FINALES
            // ================================================================
            // Los estados no finales (normales) se dibujan como círculo simple
            // Esta línea establece el estilo por defecto para los nodos siguientes
            escritor.write("  node [shape=circle];\n");

            // ================================================================
            // FLECHA HACIA EL ESTADO INICIAL
            // ================================================================
            // Crea una flecha desde el nodo invisible ("") hacia el estado inicial
            // Esto representa visualmente el estado de arranque del autómata
            escritor.write("  \"\" -> \"" + aut.getEstadoInicial() + "\";\n");

            // ================================================================
            // TRANSICIONES (FLECHAS ENTRE ESTADOS)
            // ================================================================
            // Recorre todas las transiciones del autómata
            // Estructura: transiciones[estado_origen][símbolo] = conjunto de estados_destino

            for (String origen : aut.getTransiciones().keySet()) {          // Para cada estado origen
                for (String simbolo : aut.getTransiciones().get(origen).keySet()) {  // Para cada símbolo
                    for (String destino : aut.getTransiciones().get(origen).get(simbolo)) {  // Para cada destino
                        // Escribe una línea en formato DOT: origen -> destino [label="símbolo"];
                        // El [label="..."] agrega la etiqueta con el símbolo sobre la flecha
                        escritor.write("  \"" + origen + "\" -> \"" + destino +
                                "\" [label=\"" + simbolo + "\"];\n");
                    }
                }
            }

            // ================================================================
            // CIERRE DEL ARCHIVO DOT
            // ================================================================
            escritor.write("}\n");      // Cierra la definición del grafo
            escritor.close();           // Guarda y cierra el archivo

            System.out.println("Archivo DOT generado: " + filename + ".dot");

            // ================================================================
            // PASO 2: Ejecutar GraphViz para generar la imagen PNG
            // ================================================================
            // Comando: dot -Tpng archivo.dot -o archivo.png
            // -Tpng: formato de salida PNG
            // -o: especifica el nombre del archivo de salida

            Process proceso = Runtime.getRuntime().exec(
                    "dot -Tpng " + filename + ".dot -o " + filename + ".png"
            );

            // Espera a que el proceso termine (GraphViz genere la imagen)
            int codigoSalida = proceso.waitFor();

            // Verifica si el proceso se completó correctamente
            if (codigoSalida == 0) {
                System.out.println("Imagen generada: " + filename + ".png");
            } else {
                // Si GraphViz no está instalado o hubo un error
                System.err.println("Advertencia: GraphViz no generó la imagen correctamente.");
                System.err.println("Asegúrate de tener GraphViz instalado.");
            }

        } catch (IOException e) {
            // Error al escribir el archivo .dot
            System.err.println("Error al escribir el archivo DOT: " + e.getMessage());

        } catch (InterruptedException e) {
            // El proceso fue interrumpido mientras esperaba a GraphViz
            System.err.println("Error: El proceso de GraphViz fue interrumpido.");

        } catch (Exception e) {
            // Cualquier otro error (ej: GraphViz no encontrado)
            System.err.println("Error generando el gráfico: Asegúrate de tener GraphViz instalado.");
            System.err.println("Puedes descargarlo desde: https://graphviz.org/download/");
        }
    }
}
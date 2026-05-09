import java.io.*;

/**
 * Clase que genera representaciones visuales de automatas usando GraphViz.
 *
 * GraphViz es una herramienta externa que convierte descripciones en formato DOT
 * a imagenes (PNG, PDF, SVG, etc.).
 *
 * El proceso consta de dos pasos:
 * 1. Generar un archivo .dot con la descripcion del automata en lenguaje DOT
 * 2. Ejecutar el comando "dot" de GraphViz para convertir el .dot a .png
 *
 * Para que esta clase funcione correctamente, GraphViz debe estar instalado
 * y accesible desde la linea de comandos (en el PATH del sistema).
 *
 */
public class GraphVizGenerator {

    /**
     * Genera un diagrama visual del automata usando GraphViz.
     *
     * El metodo crea dos archivos en el directorio resultados/:
     * - resultados/<filename>.dot: Descripcion del automata en formato DOT
     * - resultados/<filename>.png: Imagen generada por GraphViz
     *
     * Si el directorio resultados/ no existe, se crea automaticamente.
     *
     * @param aut El automata a visualizar (puede ser AFD o AFND)
     * @param filename Nombre base para los archivos de salida (sin extension)
     *
     * @example
     * GraphVizGenerator.generateGraph(automataMinimizado, "automaton1_minimized");
     * // Genera: resultados/automaton1_minimized.dot y resultados/automaton1_minimized.png
     */
    public static void generateGraph(Automata aut, String filename) {
        try {
            // CREAR DIRECTORIO resultados/ SI NO EXISTE
            File dirResultados = new File("resultados");
            if (!dirResultados.exists()) {
                boolean creado = dirResultados.mkdir();
                if (creado) {
                    System.out.println("Directorio 'resultados/' creado.");
                } else {
                    System.err.println("Advertencia: No se pudo crear el directorio 'resultados/'.");
                }
            }

            // PASO 1: Crear y escribir el archivo .dot
            // El archivo .dot contiene la descripcion del grafo en lenguaje DOT
            // Formato DOT: https://graphviz.org/doc/info/lang.html

            String rutaDot = "resultados/" + filename + ".dot";
            String rutaPng = "resultados/" + filename + ".png";

            File archivoDot = new File(rutaDot);
            BufferedWriter escritor = new BufferedWriter(new FileWriter(archivoDot));

            // CABECERA DEL GRAFO
            // "digraph" define un grafo dirigido (las flechas tienen direccion)
            escritor.write("digraph Automata {\n");

            // "rankdir=LR" hace que el grafo se dibuje de izquierda a derecha
            // (LR = Left to Right), lo que es estandar para automatas
            escritor.write("  rankdir=LR;\n");

            // Nodo invisible (sin forma) para la flecha que apunta al estado inicial
            // Esto crea una flecha que no viene de ningun nodo
            escritor.write("  \"\" [shape=none];\n");

            // DEFINIR FORMA DE LOS ESTADOS FINALES
            // Los estados finales se dibujan como doble circulo (doublecircle)
            // Primero se definen todos para que la configuracion se aplique correctamente
            for (String estadoFinal : aut.getEstadosFinales()) {
                escritor.write("  \"" + estadoFinal + "\" [shape=doublecircle];\n");
            }

            // DEFINIR FORMA DE LOS ESTADOS NO FINALES
            // Los estados no finales (normales) se dibujan como circulo simple
            // Esta linea establece el estilo por defecto para los nodos siguientes
            escritor.write("  node [shape=circle];\n");

            // FLECHA HACIA EL ESTADO INICIAL
            // Crea una flecha desde el nodo invisible ("") hacia el estado inicial
            // Esto representa visualmente el estado de arranque del automata
            escritor.write("  \"\" -> \"" + aut.getEstadoInicial() + "\";\n");

            // TRANSICIONES (FLECHAS ENTRE ESTADOS)
            // Recorre todas las transiciones del automata
            // Estructura: transiciones[estado_origen][simbolo] = conjunto de estados_destino

            for (String origen : aut.getTransiciones().keySet()) {          // Para cada estado origen
                for (String simbolo : aut.getTransiciones().get(origen).keySet()) {  // Para cada simbolo
                    for (String destino : aut.getTransiciones().get(origen).get(simbolo)) {  // Para cada destino
                        // Escribe una linea en formato DOT: origen -> destino [label="simbolo"];
                        // El [label="..."] agrega la etiqueta con el simbolo sobre la flecha
                        escritor.write("  \"" + origen + "\" -> \"" + destino +
                                "\" [label=\"" + simbolo + "\"];\n");
                    }
                }
            }

            // CIERRE DEL ARCHIVO DOT
            escritor.write("}\n");      // Cierra la definicion del grafo
            escritor.close();           // Guarda y cierra el archivo

            System.out.println("Archivo DOT generado: " + rutaDot);

            // PASO 2: Ejecutar GraphViz para generar la imagen PNG
            // Comando: dot -Tpng resultados/archivo.dot -o resultados/archivo.png
            // -Tpng: formato de salida PNG
            // -o: especifica el nombre del archivo de salida

            Process proceso = Runtime.getRuntime().exec(
                    "dot -Tpng " + rutaDot + " -o " + rutaPng
            );

            // Espera a que el proceso termine (GraphViz genere la imagen)
            int codigoSalida = proceso.waitFor();

            // Verifica si el proceso se completo correctamente
            if (codigoSalida == 0) {
                System.out.println("Imagen generada: " + rutaPng);
            } else {
                // Si GraphViz no esta instalado o hubo un error
                System.err.println("Advertencia: GraphViz no genero la imagen correctamente.");
                System.err.println("Asegurate de tener GraphViz instalado.");
            }

        } catch (IOException e) {
            // Error al escribir el archivo .dot
            System.err.println("Error al escribir el archivo DOT: " + e.getMessage());

        } catch (InterruptedException e) {
            // El proceso fue interrumpido mientras esperaba a GraphViz
            System.err.println("Error: El proceso de GraphViz fue interrumpido.");

        } catch (Exception e) {
            // Cualquier otro error (ej: GraphViz no encontrado)
            System.err.println("Error generando el grafico: Asegurate de tener GraphViz instalado.");
            System.err.println("Puedes descargarlo desde: https://graphviz.org/download/");
        }
    }
}
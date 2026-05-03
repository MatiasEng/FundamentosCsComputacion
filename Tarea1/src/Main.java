import java.io.*;      // Importa clases para manejo de archivos
import java.util.*;    // Importa colecciones y utilidades

/**
 * Clase principal del proyecto de Autómatas Finitos.
 * Implementa conversión de AFND a AFD, minimización, verificación de equivalencia y visualización.
 *
 * El programa realiza los siguientes pasos:
 * 1. Lee dos autómatas desde archivos de texto
 * 2. Convierte a AFD si es necesario (si alguno es AFND)
 * 3. Minimiza ambos autómatas
 * 4. Compara si son equivalentes (aceptan el mismo lenguaje)
 * 5. Genera diagramas visuales usando GraphViz
 *
 * @authors [Tus Nombres Aquí]
 * @version 1.0
 */
public class Main {

    /**
     * Método principal que ejecuta el programa.
     *
     * @param args Argumentos de línea de comandos (no se usan, los archivos son fijos)
     */
    public static void main(String[] args) {
        try {
            // ================================================================
            // CONFIGURACIÓN INICIAL: Definición de archivos de entrada
            // ================================================================

            // Archivos de entrada fijos (deben estar dentro de la carpeta "input")
            String archivo1 = "input/automaton1.txt";
            String archivo2 = "input/automaton2.txt";

            // ================================================================
            // VERIFICACIÓN DE ARCHIVOS
            // ================================================================

            System.out.println("=== Verificando archivos de entrada ===");
            File f1 = new File(archivo1);
            File f2 = new File(archivo2);

            // Comprueba si el primer archivo existe
            if (!f1.exists()) {
                System.err.println("Error: No se encuentra el archivo " + archivo1);
                System.err.println("Asegurate de crear la carpeta 'input' y colocar los archivos .txt");
                System.exit(1);  // Termina el programa con código de error
            }

            // Comprueba si el segundo archivo existe
            if (!f2.exists()) {
                System.err.println("Error: No se encuentra el archivo " + archivo2);
                System.err.println("Asegurate de crear la carpeta 'input' y colocar los archivos .txt");
                System.exit(1);  // Termina el programa con código de error
            }

            System.out.println("Archivo 1: " + archivo1 + " - OK");
            System.out.println("Archivo 2: " + archivo2 + " - OK");

            // ================================================================
            // PASO 1: LECTURA DE LOS AUTÓMATAS DESDE ARCHIVOS
            // ================================================================

            System.out.println("\n=== Paso 1: Leyendo Autómatas ===");

            // Lee el primer autómata usando el método estático de la clase Automata
            Automata aut1 = Automata.leerDesdeArchivo(archivo1);
            // Lee el segundo autómata
            Automata aut2 = Automata.leerDesdeArchivo(archivo2);

            // Muestra el tipo de cada autómata (AFD o AFND)
            System.out.println("Autómata 1: " + aut1.getType());
            System.out.println("Autómata 2: " + aut2.getType());

            // ================================================================
            // MOSTRAR INFORMACIÓN DETALLADA DE LOS AUTÓMATAS (para depuración)
            // ================================================================

            System.out.println("\n--- Detalles del Autómata 1 ---");
            System.out.println("Estados: " + aut1.getEstados());
            System.out.println("Alfabeto: " + aut1.getAlfabeto());
            System.out.println("Estado inicial: " + aut1.getEstadoInicial());
            System.out.println("Estados finales: " + aut1.getEstadosFinales());

            System.out.println("\n--- Detalles del Autómata 2 ---");
            System.out.println("Estados: " + aut2.getEstados());      // CORREGIDO: antes mostraba aut1
            System.out.println("Alfabeto: " + aut2.getAlfabeto());    // CORREGIDO: antes mostraba aut1
            System.out.println("Estado inicial: " + aut2.getEstadoInicial()); // CORREGIDO
            System.out.println("Estados finales: " + aut2.getEstadosFinales()); // CORREGIDO

            // ================================================================
            // PASO 2: CONVERSIÓN A AFD (si es necesario)
            // ================================================================

            System.out.println("\n=== Paso 2: Convirtiendo a AFD ===");

            // Convierte el primer autómata a AFD (si ya es AFD, queda igual)
            Automata afd1 = AutomataConverter.toDFA(aut1);
            // Convierte el segundo autómata a AFD
            Automata afd2 = AutomataConverter.toDFA(aut2);

            System.out.println("Después de la conversión:");
            System.out.println("Autómata 1: " + afd1.getType());
            System.out.println("Autómata 2: " + afd2.getType());

            // ================================================================
            // PASO 3: MINIMIZACIÓN Y VERIFICACIÓN DE EQUIVALENCIA
            // ================================================================

            System.out.println("\n=== Paso 3: Minimizando y Verificando Equivalencia ===");

            // Minimiza el primer AFD usando el algoritmo de tabla de llenado
            Automata min1 = AutomataMinimizer.minimize(afd1);
            // Minimiza el segundo AFD
            Automata min2 = AutomataMinimizer.minimize(afd2);

            // Compara los dos autómatas minimizados para ver si son equivalentes
            // Dos autómatas son equivalentes si aceptan exactamente el mismo lenguaje
            boolean equivalentes = AutomataEquivalenceChecker.areEquivalent(min1, min2);

            // Muestra el resultado de la comparación
            System.out.println("\n==========================================");
            if (equivalentes) {
                System.out.println("RESULTADO: Los autómatas son EQUIVALENTES");
                System.out.println("Ambos autómatas aceptan el mismo lenguaje.");
            } else {
                System.out.println("RESULTADO: Los autómatas NO son EQUIVALENTES");
                System.out.println("Los autómatas aceptan lenguajes diferentes.");
            }
            System.out.println("==========================================\n");

            // ================================================================
            // PASO 4: GENERACIÓN DE VISUALIZACIONES CON GRAPHVIZ
            // ================================================================

            System.out.println("=== Paso 4: Generando Visualizaciones ===");

            // Genera el diagrama del primer autómata minimizado en formato DOT y PNG
            // El archivo de salida será "automaton1_minimized.dot" y ".png"
            GraphVizGenerator.generateGraph(min1, "automaton1_minimized");

            // Genera el diagrama del segundo autómata minimizado
            GraphVizGenerator.generateGraph(min2, "automaton2_minimized");

            // ================================================================
            // FINALIZACIÓN DEL PROGRAMA
            // ================================================================

            System.out.println("\n=== Proceso Completado ===");
            System.out.println("Para ver los diagramas:");
            System.out.println("  - Archivos DOT generados: automaton1_minimized.dot, automaton2_minimized.dot");
            System.out.println("  - Imágenes PNG: automaton1_minimized.png, automaton2_minimized.png");
            System.out.println("  - Si GraphViz está instalado, las imágenes se generaron automáticamente.");
            System.out.println("  - Si no, puedes generar las imágenes manualmente con: dot -Tpng archivo.dot -o archivo.png");

        } catch (Exception e) {
            // Captura cualquier excepción no manejada y muestra el error
            System.err.println("Error durante la ejecución: " + e.getMessage());
            e.printStackTrace();  // Muestra la traza completa del error para depuración
        }
    }
}
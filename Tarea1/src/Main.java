import java.io.*;
import java.util.*;

/**
 * Clase principal del proyecto de Automatas Finitos.
 * Implementa conversion de AFND a AFD, minimizacion, verificacion de equivalencia y visualizacion.
 *
 * El programa realiza los siguientes pasos:
 * 1. Lee dos automatas desde archivos de texto
 * 2. Convierte a AFD si es necesario (si alguno es AFND)
 * 3. Minimiza ambos automatas
 * 4. Compara si son equivalentes (aceptan el mismo lenguaje)
 * 5. Genera diagramas visuales usando GraphViz (original, AFD, minimizado)
 *
 */
public class Main {

    /**
     * Metodo principal que ejecuta el programa.
     * Uso: java Main [archivo1 archivo2]
     * Si no se proporcionan argumentos, se usan los archivos por defecto:
     * input/automaton1.txt y input/automaton2.txt.
     * Si se proporcionan dos argumentos, se antepone la carpeta "input/" a cada uno,
     * a menos que el argumento ya contenga una ruta (contenga '/' o '\').
     *
     * @param args Argumentos de linea de comandos: [archivo1 archivo2] (opcionales)
     */
    public static void main(String[] args) {
        try {
            // CONFIGURACION INICIAL: Determinar nombres de archivos
            String nombreArchivo1, nombreArchivo2;

            if (args.length >= 2) {
                nombreArchivo1 = args[0];
                nombreArchivo2 = args[1];
                System.out.println("Usando archivos especificados en argumentos:");
                System.out.println("  Archivo 1: " + nombreArchivo1);
                System.out.println("  Archivo 2: " + nombreArchivo2);
            } else {
                System.out.println("No se proporcionaron argumentos. Usando archivos por defecto.");
                nombreArchivo1 = "automata1.txt";
                nombreArchivo2 = "automata2.txt";
                System.out.println("  Archivo 1: " + nombreArchivo1);
                System.out.println("  Archivo 2: " + nombreArchivo2);
            }

            // Busca la carpeta "input". Si estamos dentro de "src", retrocede un nivel ("../input")
            String directorio = "input";
            if (!new File(directorio).exists() && new File(".." + File.separator + "input").exists()) {
                directorio = ".." + File.separator + "input";
            }

            String rutaArchivo1 = (nombreArchivo1.contains("/") || nombreArchivo1.contains("\\")) ?
                    nombreArchivo1 : directorio + File.separator + nombreArchivo1;
            String rutaArchivo2 = (nombreArchivo2.contains("/") || nombreArchivo2.contains("\\")) ?
                    nombreArchivo2 : directorio + File.separator + nombreArchivo2;

            // VERIFICACION DE ARCHIVOS
            System.out.println("\n=== Verificando archivos de entrada ===");
            File f1 = new File(rutaArchivo1);
            File f2 = new File(rutaArchivo2);

            if (!f1.exists()) {
                System.err.println("Error: No se encuentra el archivo " + rutaArchivo1);
                System.err.println("Asegurate de que el archivo exista en la carpeta 'input'.");
                System.exit(1);
            }
            if (!f2.exists()) {
                System.err.println("Error: No se encuentra el archivo " + rutaArchivo2);
                System.err.println("Asegurate de que el archivo exista en la carpeta 'input'.");
                System.exit(1);
            }

            System.out.println("Archivo 1: " + rutaArchivo1 + " - OK");
            System.out.println("Archivo 2: " + rutaArchivo2 + " - OK");

            // PASO 1: LECTURA DE LOS AUTOMATAS DESDE ARCHIVOS

            System.out.println("\n=== Paso 1: Leyendo Automatas ===");
            Automata aut1 = Automata.leerDesdeArchivo(rutaArchivo1);
            Automata aut2 = Automata.leerDesdeArchivo(rutaArchivo2);

            System.out.println("Automata 1: " + aut1.getType());
            System.out.println("Automata 2: " + aut2.getType());

            // MOSTRAR INFORMACION DETALLADA (opcional)

            System.out.println("\n--- Detalles del Automata 1 ---");
            System.out.println("Estados: " + aut1.getEstados());
            System.out.println("Alfabeto: " + aut1.getAlfabeto());
            System.out.println("Estado inicial: " + aut1.getEstadoInicial());
            System.out.println("Estados finales: " + aut1.getEstadosFinales());

            System.out.println("\n--- Detalles del Automata 2 ---");
            System.out.println("Estados: " + aut2.getEstados());
            System.out.println("Alfabeto: " + aut2.getAlfabeto());
            System.out.println("Estado inicial: " + aut2.getEstadoInicial());
            System.out.println("Estados finales: " + aut2.getEstadosFinales());

            // GENERAR DIAGRAMAS DE LOS AUTOMATAS ORIGINALES

            System.out.println("\n=== Generando diagramas de los automatas originales ===");
            GraphVizGenerator.generateGraph(aut1, "automata1_original");
            GraphVizGenerator.generateGraph(aut2, "automata2_original");

            // PASO 2: CONVERSION A AFD (si es necesario)

            System.out.println("\n=== Paso 2: Convirtiendo a AFD ===");
            Automata afd1 = AutomataConverter.toDFA(aut1);
            Automata afd2 = AutomataConverter.toDFA(aut2);

            System.out.println("Despues de la conversion:");
            System.out.println("Automata 1: " + afd1.getType());
            System.out.println("Automata 2: " + afd2.getType());

            // GENERAR DIAGRAMAS DE LOS AFD CONVERTIDOS (no minimizados)

            System.out.println("\n=== Generando diagramas de los AFD convertidos ===");
            GraphVizGenerator.generateGraph(afd1, "automata1_afd");
            GraphVizGenerator.generateGraph(afd2, "automata2_afd");

            // PASO 3: MINIMIZACION Y VERIFICACION DE EQUIVALENCIA

            System.out.println("\n=== Paso 3: Minimizando y Verificando Equivalencia ===");
            Automata min1 = AutomataMinimizer.minimize(afd1);
            Automata min2 = AutomataMinimizer.minimize(afd2);

            boolean equivalentes = AutomataEquivalenceChecker.areEquivalent(min1, min2);

            System.out.println("\n==========================================");
            if (equivalentes) {
                System.out.println("RESULTADO: Los automatas son EQUIVALENTES");
                System.out.println("Ambos automatas aceptan el mismo lenguaje.");
            } else {
                System.out.println("RESULTADO: Los automatas NO son EQUIVALENTES");
                System.out.println("Los automatas aceptan lenguajes diferentes.");
            }
            System.out.println("==========================================\n");

            // GENERAR DIAGRAMAS DE LOS AUTOMATAS MINIMIZADOS

            System.out.println("=== Generando diagramas de los automatas minimizados ===");
            GraphVizGenerator.generateGraph(min1, "automata1_minimizado");
            GraphVizGenerator.generateGraph(min2, "automata2_minimizado");

            // FINALIZACION

            System.out.println("\n=== Proceso Completado ===");
            System.out.println("Archivos generados (3 versiones por automata):");
            System.out.println("  Automata 1:");
            System.out.println("    - automata1_original.dot/.png");
            System.out.println("    - automata1_afd.dot/.png");
            System.out.println("    - automata1_minimizado.dot/.png");
            System.out.println("  Automata 2:");
            System.out.println("    - automata2_original.dot/.png");
            System.out.println("    - automata2_afd.dot/.png");
            System.out.println("    - automata2_minimizado.dot/.png");
            System.out.println("Si GraphViz no genero las imagenes, instala o ejecuta manualmente:");
            System.out.println("  dot -Tpng archivo.dot -o archivo.png");

        } catch (Exception e) {
            System.err.println("Error durante la ejecucion: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
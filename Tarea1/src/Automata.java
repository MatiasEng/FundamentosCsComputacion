import java.io.*;
import java.util.*;

/**
 * Clase que representa un Autómata Finito (AFD o AFND).
 * Puede ser un Autómata Finito Determinista (AFD) o No Determinista (AFND).
 *
 * @authors [Tus Nombres Aquí]
 * @version 1.0
 */
public class Automata {
    // ==================== ATRIBUTOS ====================

    private Set<String> estados;                                      // Conjunto de estados (K)
    private Set<String> alfabeto;                                    // Alfabeto o símbolos de entrada (Sigma)
    private Map<String, Map<String, Set<String>>> transiciones;      // Función de transición (delta) - soporta AFND
    private String estadoInicial;                                    // Estado inicial (s)
    private Set<String> estadosFinales;                              // Conjunto de estados finales o de aceptación (F)
    private boolean esAFD;                                          // true = AFD, false = AFND

    // ==================== CONSTRUCTOR ====================

    /**
     * Constructor por defecto. Inicializa todas las estructuras vacías.
     * Por defecto asume que es un AFD hasta que se demuestre lo contrario.
     */
    public Automata() {
        this.estados = new HashSet<>();           // Inicializa conjunto de estados vacío
        this.alfabeto = new HashSet<>();          // Inicializa alfabeto vacío
        this.transiciones = new HashMap<>();      // Inicializa mapa de transiciones vacío
        this.estadosFinales = new HashSet<>();    // Inicializa conjunto de estados finales vacío
        this.esAFD = true;                        // Asume que es AFD inicialmente
    }

    // ==================== MÉTODOS PRINCIPALES ====================

    /**
     * Lee un autómata desde un archivo de texto con el formato específico:
     *
     * Formato esperado:
     * K={q0,q1,q2}          (Estados)
     * Sigma={a,b}           (Alfabeto)
     * delta:                (Inicio de transiciones)
     * (q0,a,q0)             (Transición: desde, símbolo, hacia)
     * (q0,b,q1)
     * (q0,ε,q2)             (Transición épsilon - solo para AFND)
     * s=q0                  (Estado inicial)
     * F={q1}                (Estados finales)
     *
     * @param nombreArchivo Ruta del archivo a leer
     * @return Objeto Automata construido desde el archivo
     * @throws IOException Si ocurre un error al leer el archivo
     */
    public static Automata leerDesdeArchivo(String nombreArchivo) throws IOException {
        Automata aut = new Automata();                    // Crea un nuevo autómata vacío
        BufferedReader lector = new BufferedReader(new FileReader(nombreArchivo));  // Abre el archivo
        String linea;                                     // Variable para almacenar cada línea
        boolean leyendoDelta = false;                     // Bandera que indica si estamos leyendo transiciones

        // Bucle principal: lee el archivo línea por línea
        while ((linea = lector.readLine()) != null) {
            linea = linea.trim();                         // Elimina espacios en blanco
            if (linea.isEmpty()) continue;                // Salta líneas vacías

            // ===== LECTURA DE ESTADOS =====
            if (linea.startsWith("K=") || linea.startsWith("k=")) {
                // Ejemplo: "K={q0,q1,q2}" -> extraer "q0,q1,q2"
                String estadosStr = linea.substring(2);                    // Elimina "K="
                estadosStr = estadosStr.substring(1, estadosStr.length() - 1); // Elimina las llaves { }
                String[] arregloEstados = estadosStr.split(",");            // Separa por comas
                for (String estado : arregloEstados) {                      // Recorre cada estado
                    aut.estados.add(estado.trim());                         // Lo agrega al conjunto
                }
            }

            // ===== LECTURA DEL ALFABETO =====
            else if (linea.startsWith("Sigma=") || linea.startsWith("sigma=")) {
                // Ejemplo: "Sigma={a,b}" -> extraer "a,b"
                String sigmaStr = linea.substring(6);                      // Elimina "Sigma="
                sigmaStr = sigmaStr.substring(1, sigmaStr.length() - 1);   // Elimina las llaves { }
                String[] arregloSimbolos = sigmaStr.split(",");            // Separa por comas
                for (String simbolo : arregloSimbolos) {                   // Recorre cada símbolo
                    aut.alfabeto.add(simbolo.trim());                      // Lo agrega al alfabeto
                }
            }

            // ===== INICIO DE LECTURA DE TRANSICIONES =====
            else if (linea.startsWith("delta:")) {
                leyendoDelta = true;                                       // Activa la bandera
            }

            // ===== LECTURA DE CADA TRANSICIÓN =====
            else if (leyendoDelta && linea.startsWith("(")) {
                // Ejemplo: "(q0,a,q0)" o "(q0,ε,q1)" -> procesar la transición
                linea = linea.substring(1, linea.length() - 1);            // Elimina los paréntesis ( )
                String[] partes = linea.split(",");                        // Separa por comas: [q0, a, q0]
                if (partes.length == 3) {                                  // Verifica que tenga 3 partes
                    String desde = partes[0].trim();        // Estado origen
                    String simbolo = partes[1].trim();      // Símbolo de transición (puede ser "ε")
                    String hacia = partes[2].trim();        // Estado destino
                    aut.agregarTransicion(desde, simbolo, hacia);  // Agrega la transición
                }
            }

            // ===== LECTURA DEL ESTADO INICIAL =====
            else if (linea.startsWith("s=")) {
                // Ejemplo: "s=q0" -> extraer "q0"
                aut.estadoInicial = linea.substring(2).trim();
            }

            // ===== LECTURA DE ESTADOS FINALES =====
            else if (linea.startsWith("F=") || linea.startsWith("f=")) {
                // Ejemplo: "F={q1}" o "F={q1,q2}" -> extraer estados finales
                String finalesStr = linea.substring(2);                    // Elimina "F="
                finalesStr = finalesStr.substring(1, finalesStr.length() - 1); // Elimina llaves { }
                if (!finalesStr.isEmpty()) {                               // Si hay estados finales
                    String[] arregloFinales = finalesStr.split(",");       // Separa por comas
                    for (String estadoFinal : arregloFinales) {            // Recorre cada estado final
                        aut.estadosFinales.add(estadoFinal.trim());        // Lo agrega al conjunto
                    }
                }
            }
        }

        lector.close();                        // Cierra el archivo
        aut.determinarTipo();                  // Determina si es AFD o AFND
        return aut;                            // Retorna el autómata construido
    }

    /**
     * Agrega una transición al autómata.
     * La estructura de transiciones permite múltiples destinos para el mismo
     * estado y símbolo (necesario para AFND).
     *
     * Estructura: transiciones[estado_origen][símbolo] = conjunto de estados_destino
     *
     * @param desde Estado origen de la transición
     * @param simbolo Símbolo que dispara la transición (puede ser "ε" para épsilon)
     * @param hacia Estado destino de la transición
     */
    public void agregarTransicion(String desde, String simbolo, String hacia) {
        // Si el estado origen no existe en el mapa, lo crea con un HashMap vacío
        transiciones.putIfAbsent(desde, new HashMap<>());

        // Si el símbolo no existe para ese estado origen, crea un nuevo conjunto de destinos
        transiciones.get(desde).putIfAbsent(simbolo, new HashSet<>());

        // Agrega el estado destino al conjunto de destinos para (origen, símbolo)
        transiciones.get(desde).get(simbolo).add(hacia);
    }

    /**
     * Determina si el autómata es AFD (Automata Finito Determinista) o AFND (Automata Finito No Determinista).
     *
     * Un autómata es NO determinista (AFND) si cumple ALGUNA de estas condiciones:
     * 1. TIENE TRANSICIONES ÉPSILON (ε) - esto lo hace automáticamente AFND
     * 2. Un mismo estado tiene múltiples transiciones con el mismo símbolo
     * 3. Faltan transiciones para algún símbolo del alfabeto en algún estado
     *
     * Si no cumple ninguna condición, es un AFD.
     */
    private void determinarTipo() {
        // ===== VERIFICACIÓN 1: Buscar transiciones épsilon (ε) =====
        // Las transiciones épsilon son una característica exclusiva de los AFND
        // Permiten cambiar de estado sin consumir ningún símbolo de entrada
        for (String estado : transiciones.keySet()) {
            Map<String, Set<String>> transicionesDelEstado = transiciones.get(estado);
            // Si existe alguna transición con el símbolo "ε" (épsilon)
            if (transicionesDelEstado.containsKey("ε")) {
                esAFD = false;    // Marca como AFND (no determinista)
                return;           // Termina el método inmediatamente
            }
        }

        // ===== VERIFICACIÓN 2: Buscar múltiples transiciones con el mismo símbolo =====
        // En un AFD, desde cada estado y con cada símbolo SOLO puede haber UNA transición
        // Si hay más de un destino posible, es AFND
        for (String estado : transiciones.keySet()) {
            Map<String, Set<String>> transicionesDelEstado = transiciones.get(estado);
            for (String simbolo : transicionesDelEstado.keySet()) {
                // Si el conjunto de destinos tiene más de 1 estado, es NO determinista
                if (transicionesDelEstado.get(simbolo).size() > 1) {
                    esAFD = false;    // Marca como AFND
                    return;           // Termina el método
                }
            }
        }

        // ===== VERIFICACIÓN 3: Buscar transiciones faltantes =====
        // En un AFD completo, para CADA estado y CADA símbolo del alfabeto DEBE existir UNA transición
        // Si falta alguna transición, es AFND (o AFD incompleto, que tratamos como AFND)
        for (String estado : estados) {
            // Obtiene las transiciones del estado (si no tiene, usa mapa vacío)
            Map<String, Set<String>> transicionesDelEstado = transiciones.getOrDefault(estado, new HashMap<>());
            // Para cada símbolo del alfabeto
            for (String simbolo : alfabeto) {
                // Si no existe transición para este símbolo, es NO determinista
                if (!transicionesDelEstado.containsKey(simbolo)) {
                    esAFD = false;    // Marca como AFND
                    return;           // Termina el método
                }
            }
        }

        // Si pasó todas las verificaciones, el autómata es un AFD
        // esAFD ya es true desde el constructor, así que no hay que cambiarlo
    }

    /**
     * Retorna el tipo de autómata como texto.
     * @return "AFD" si es determinista, "AFND" si es no determinista
     */
    public String getType() {
        return esAFD ? "AFD" : "AFND";
    }

    /**
     * Verifica si el autómata es determinista.
     * @return true si es AFD, false si es AFND
     */
    public boolean esAFD() {
        return esAFD;
    }

    // ==================== MÉTODOS GETTER ====================

    /**
     * Obtiene el conjunto de estados del autómata.
     * @return Set de Strings con los nombres de los estados
     */
    public Set<String> getEstados() {
        return estados;
    }

    /**
     * Obtiene el alfabeto del autómata.
     * @return Set de Strings con los símbolos del alfabeto
     */
    public Set<String> getAlfabeto() {
        return alfabeto;
    }

    /**
     * Obtiene el mapa de transiciones del autómata.
     * Estructura: [origen][símbolo] = conjunto de destinos
     * @return Mapa anidado con todas las transiciones
     */
    public Map<String, Map<String, Set<String>>> getTransiciones() {
        return transiciones;
    }

    /**
     * Obtiene el estado inicial del autómata.
     * @return String con el nombre del estado inicial
     */
    public String getEstadoInicial() {
        return estadoInicial;
    }

    /**
     * Obtiene el conjunto de estados finales.
     * @return Set de Strings con los nombres de los estados finales
     */
    public Set<String> getEstadosFinales() {
        return estadosFinales;
    }

    // ==================== MÉTODOS SETTER ====================

    /**
     * Establece el conjunto de estados del autómata.
     * @param estados Nuevo conjunto de estados
     */
    public void setEstados(Set<String> estados) {
        this.estados = estados;
    }

    /**
     * Establece el alfabeto del autómata.
     * @param alfabeto Nuevo conjunto de símbolos del alfabeto
     */
    public void setAlfabeto(Set<String> alfabeto) {
        this.alfabeto = alfabeto;
    }

    /**
     * Establece el mapa de transiciones del autómata.
     * @param transiciones Nuevo mapa de transiciones
     */
    public void setTransiciones(Map<String, Map<String, Set<String>>> transiciones) {
        this.transiciones = transiciones;
    }

    /**
     * Establece el estado inicial del autómata.
     * @param estadoInicial Nombre del nuevo estado inicial
     */
    public void setEstadoInicial(String estadoInicial) {
        this.estadoInicial = estadoInicial;
    }

    /**
     * Establece el conjunto de estados finales.
     * @param estadosFinales Nuevo conjunto de estados finales
     */
    public void setEstadosFinales(Set<String> estadosFinales) {
        this.estadosFinales = estadosFinales;
    }

    /**
     * Establece si el autómata es determinista o no.
     * @param esAFD true para AFD, false para AFND
     */
    public void setEsAFD(boolean esAFD) {
        this.esAFD = esAFD;
    }
}
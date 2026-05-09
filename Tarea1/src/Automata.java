import java.io.*;
import java.util.*;

/**
 * Clase que representa un Automata Finito (AFD o AFND).
 * Puede ser un Automata Finito Determinista (AFD) o No Determinista (AFND).
 *
 */
public class Automata {
    // Atributos
    private Set<String> estados;                                      // Conjunto de estados (K)
    private Set<String> alfabeto;                                    // Alfabeto o simbolos de entrada (Sigma)
    private Map<String, Map<String, Set<String>>> transiciones;      // Funcion de transicion (delta) - soporta AFND
    private String estadoInicial;                                    // Estado inicial (s)
    private Set<String> estadosFinales;                              // Conjunto de estados finales o de aceptacion (F)
    private boolean esAFD;                                          // true = AFD, false = AFND


    /**
     * Constructor:
     * Constructor por defecto. Inicializa todo vacio
     * Por defecto asume que es un AFD hasta que se demuestre lo contrario.
     */
    public Automata() {
        this.estados = new HashSet<>();           // Inicializa conjunto de estados vacio
        this.alfabeto = new HashSet<>();          // Inicializa alfabeto vacio
        this.transiciones = new HashMap<>();      // Inicializa mapa de transiciones vacio
        this.estadosFinales = new HashSet<>();    // Inicializa conjunto de estados finales vacio
        this.esAFD = true;                        // Asume que es AFD inicialmente
    }

    // METODOS PRINCIPALES

    /**
     * Lee un automata desde un archivo de texto con el formato especifico:
     *
     * Formato esperado: (del enunciado)
     * K={q0,q1,q2}          (Estados)
     * Sigma={a,b}           (Alfabeto)
     * delta:                (Inicio de transiciones)
     * (q0,a,q0)             (Transicion: desde, simbolo, hacia)
     * (q0,b,q1)
     * (q0,eps,q2)           (Transicion epsilon - solo para AFND)
     * s=q0                  (Estado inicial)
     * F={q1}                (Estados finales)
     *
     * @param nombreArchivo Ruta del archivo a leer
     * @return Objeto Automata construido desde el archivo
     * @throws IOException Si ocurre un error al leer el archivo
     */
    public static Automata leerDesdeArchivo(String nombreArchivo) throws IOException {
        Automata aut = new Automata();                    // Crea un nuevo automata vacio
        BufferedReader lector = new BufferedReader(new FileReader(nombreArchivo));  // Abre el archivo
        String linea;                                     // Variable para almacenar cada linea
        boolean leyendoDelta = false;                     // Bandera que indica si estamos leyendo transiciones

        // Bucle principal: lee el archivo linea por linea
        while ((linea = lector.readLine()) != null) {
            linea = linea.trim();                         // Elimina espacios en blanco
            if (linea.isEmpty()) continue;                // Salta lineas vacias

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
                for (String simbolo : arregloSimbolos) {                   // Recorre cada simbolo
                    aut.alfabeto.add(simbolo.trim());                      // Lo agrega al alfabeto
                }
            }

            // ===== INICIO DE LECTURA DE TRANSICIONES =====
            else if (linea.startsWith("delta:")) {
                leyendoDelta = true;                                       // Activa la bandera
            }

            // ===== LECTURA DE CADA TRANSICION =====
            else if (leyendoDelta && linea.startsWith("(")) {
                // Ejemplo: "(q0,a,q0)" o "(q0,ε,q1)" -> procesar la transicion
                linea = linea.substring(1, linea.length() - 1);            // Elimina los parentesis ( )
                String[] partes = linea.split(",");                        // Separa por comas: [q0, a, q0]
                if (partes.length == 3) {                                  // Verifica que tenga 3 partes
                    String desde = partes[0].trim();        // Estado origen
                    String simbolo = partes[1].trim();      // Simbolo de transicion (puede ser "ε")
                    String hacia = partes[2].trim();        // Estado destino
                    aut.agregarTransicion(desde, simbolo, hacia);  // Agrega la transicion
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
        return aut;                            // Retorna el automata construido
    }

    /**
     * Agrega una transicion al automata.
     * La estructura de transiciones permite multiples destinos para el mismo
     * estado y simbolo (necesario para AFND).
     *
     * Estructura: transiciones[estado_origen][simbolo] = conjunto de estados_destino
     *
     * @param desde Estado origen de la transicion
     * @param simbolo Simbolo que dispara la transicion (puede ser "ε" para epsilon)
     * @param hacia Estado destino de la transicion
     */
    public void agregarTransicion(String desde, String simbolo, String hacia) {
        // Si el estado origen no existe en el mapa, lo crea con un HashMap vacio
        transiciones.putIfAbsent(desde, new HashMap<>());

        // Si el simbolo no existe para ese estado origen, crea un nuevo conjunto de destinos
        transiciones.get(desde).putIfAbsent(simbolo, new HashSet<>());

        // Agrega el estado destino al conjunto de destinos para (origen, simbolo)
        transiciones.get(desde).get(simbolo).add(hacia);
    }

    /**
     * Determina si el automata es AFD (Automata Finito Determinista) o AFND (Automata Finito No Determinista).
     *
     * Un automata es NO determinista (AFND) si cumple ALGUNA de estas condiciones:
     * 1. TIENE TRANSICIONES EPSILON (eps) - esto lo hace automaticamente AFND
     * 2. Un mismo estado tiene multiples transiciones con el mismo simbolo del alfabeto
     * 3. Faltan transiciones para algun simbolo del alfabeto en algun estado
     *
     * Si no cumple ninguna condicion, es un AFD.
     */
    private void determinarTipo() {
        // ===== VERIFICACION 1: Buscar transiciones epsilon (ε) =====
        // Las transiciones epsilon son una caracteristica exclusiva de los AFND
        // Permiten cambiar de estado sin consumir ningun simbolo de entrada
        for (String estado : transiciones.keySet()) {
            Map<String, Set<String>> transicionesDelEstado = transiciones.get(estado);
            // Si existe alguna transicion con el simbolo "ε" (epsilon)
            if (transicionesDelEstado.containsKey("eps")) {
                esAFD = false;    // Marca como AFND (no determinista)
                return;           // Termina el metodo inmediatamente
            }
        }

        // ===== VERIFICACION 2: Buscar multiples transiciones con el mismo simbolo =====
        // En un AFD, desde cada estado y con cada simbolo SOLO puede haber UNA transicion
        // Si hay mas de un destino posible, es AFND
        for (String estado : transiciones.keySet()) {
            Map<String, Set<String>> transicionesDelEstado = transiciones.get(estado);
            for (String simbolo : transicionesDelEstado.keySet()) {
                // Si el conjunto de destinos tiene mas de 1 estado, es NO determinista
                if (transicionesDelEstado.get(simbolo).size() > 1) {
                    esAFD = false;    // Marca como AFND
                    return;           // Termina el metodo
                }
            }
        }

        // ===== VERIFICACION 3: Buscar transiciones faltantes =====
        // En un AFD completo, para CADA estado y CADA simbolo del alfabeto DEBE existir UNA transicion
        // Si falta alguna transicion, es AFND (o AFD incompleto, que tratamos como AFND)
        for (String estado : estados) {
            // Obtiene las transiciones del estado (si no tiene, usa mapa vacio)
            Map<String, Set<String>> transicionesDelEstado = transiciones.getOrDefault(estado, new HashMap<>());
            // Para cada simbolo del alfabeto
            for (String simbolo : alfabeto) {
                // Si no existe transicion para este simbolo, es NO determinista
                if (!transicionesDelEstado.containsKey(simbolo)) {
                    esAFD = false;    // Marca como AFND
                    return;           // Termina el metodo
                }
            }
        }

        // Si paso todas las verificaciones, el automata es un AFD
        // esAFD ya es true desde el constructor, asi que no hay que cambiarlo
    }

    /**
     * Retorna el tipo de automata como texto.
     */
    public String getType() {
        return esAFD ? "AFD" : "AFND";
    }

    /**
     * Verifica si el automata es determinista.
     */
    public boolean esAFD() {
        return esAFD;
    }

    /**
     * Obtiene el conjunto de estados del automata.
     */
    public Set<String> getEstados() {
        return estados;
    }

    /**
     * Obtiene el alfabeto del automata.
     */
    public Set<String> getAlfabeto() {
        return alfabeto;
    }

    /**
     * Obtiene el mapa de transiciones del automata.
     * Estructura: [origen][simbolo] = conjunto de destinos
     */
    public Map<String, Map<String, Set<String>>> getTransiciones() {
        return transiciones;
    }

    /**
     * Obtiene el estado inicial del automata.
     */
    public String getEstadoInicial() {
        return estadoInicial;
    }

    /**
     * Obtiene el conjunto de estados finales.
     */
    public Set<String> getEstadosFinales() {
        return estadosFinales;
    }

    // ==================== METODOS SETTER ====================

    /**
     * Establece el conjunto de estados del automata.
     */
    public void setEstados(Set<String> estados) {
        this.estados = estados;
    }

    /**
     * Establece el alfabeto del automata.
     */
    public void setAlfabeto(Set<String> alfabeto) {
        this.alfabeto = alfabeto;
    }

    /**
     * Establece el mapa de transiciones del automata.
     */
    public void setTransiciones(Map<String, Map<String, Set<String>>> transiciones) {
        this.transiciones = transiciones;
    }

    /**
     * Establece el estado inicial del automata.
     */
    public void setEstadoInicial(String estadoInicial) {
        this.estadoInicial = estadoInicial;
    }

    /**
     * Establece el conjunto de estados finales.
     */
    public void setEstadosFinales(Set<String> estadosFinales) {
        this.estadosFinales = estadosFinales;
    }

    /**
     * Establece si el automata es determinista o no.
     */
    public void setEsAFD(boolean esAFD) {
        this.esAFD = esAFD;
    }
}
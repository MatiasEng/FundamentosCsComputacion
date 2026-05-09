import java.util.*;    // Importa colecciones (List, Set, Map, ArrayList, HashSet, etc.)

/**
 * Clase que convierte un AFND (Automata Finito No Determinista) a un AFD (Automata Finito Determinista)
 * utilizando el algoritmo de construccion de subconjuntos (subset construction).
 *
 * El algoritmo tambien se conoce como "powerset construction" o "determinizacion".
 */
public class AutomataConverter {

    /**
     * Convierte un AFND a un AFD equivalente.
     *
     * El algoritmo funciona de la siguiente manera:
     * 1. Calcula la clausura epsilon del estado inicial
     * 2. Para cada subconjunto de estados encontrado:
     *    - Para cada simbolo del alfabeto, calcula la transicion
     *    - Agrega nuevos subconjuntos si no existen
     * 3. Los estados del AFD son subconjuntos de estados del AFND
     * 4. Un estado del AFD es final si contiene al menos un estado final del AFND
     * 5. Se agrega un estado "trampa" si hay transiciones faltantes
     *
     * @param afnd El automata no determinista a convertir (puede tener transiciones epsilon)
     * @return Un nuevo AFD equivalente al AFND de entrada
     */
    public static Automata toDFA(Automata afnd) {
        // CASO BASE: Si ya es AFD, no necesita conversion
        if (afnd.esAFD()) {
            return afnd;
        }

        // INICIALIZACION: Crear el nuevo AFD vacio
        Automata afd = new Automata();
        afd.setAlfabeto(new HashSet<>(afnd.getAlfabeto()));    // Copia el alfabeto del AFND

        // ESTRUCTURAS DE DATOS PARA LA CONSTRUCCION DE SUBCONJUNTOS

        // Mapa: subconjunto de estados del AFND -> nombre del estado en el AFD
        Map<Set<String>, String> subconjuntoANombre = new HashMap<>();

        // Mapa: nombre del estado en el AFD -> subconjunto de estados del AFND
        Map<String, Set<String>> nombreASubconjunto = new HashMap<>();

        // Lista de subconjuntos pendientes por procesar
        List<Set<String>> subconjuntosPendientes = new ArrayList<>();

        // ----------------------------------------------------------------------------
        // PASO 1: Calcular la clausura epsilon del estado inicial

        // La clausura epsilon incluye el estado inicial mas todos los estados
        // alcanzables mediante transiciones epsilon (ε)
        Set<String> subconjuntoInicial = clausuraEpsilon(afnd, Set.of(afnd.getEstadoInicial()));

        // Genera un nombre unico para este subconjunto (ej: "{q0,q1}")
        String nombreInicial = obtenerNombreSubconjunto(subconjuntoInicial);

        // Registra las correspondencias
        subconjuntoANombre.put(subconjuntoInicial, nombreInicial);
        nombreASubconjunto.put(nombreInicial, subconjuntoInicial);

        // Agrega a la lista de pendientes para procesar
        subconjuntosPendientes.add(subconjuntoInicial);

        // Establece el estado inicial del AFD
        afd.setEstadoInicial(nombreInicial);


        // ESTRUCTURA PARA ALMACENAR LAS TRANSICIONES DEL AFD
        // Mapa: estado_origen -> (simbolo -> estado_destino)
        Map<String, Map<String, String>> transicionesAFD = new HashMap<>();

        // PASO 2: Procesar todos los subconjuntos (algoritmo de construccion)
        while (!subconjuntosPendientes.isEmpty()) {
            // Toma el primer subconjunto pendiente (FIFO - como una cola)
            Set<String> subconjunto = subconjuntosPendientes.remove(0);
            String estadoOrigen = subconjuntoANombre.get(subconjunto);

            // Inicializa el mapa de transiciones para este estado origen
            transicionesAFD.putIfAbsent(estadoOrigen, new HashMap<>());

            // Para cada simbolo del alfabeto, calcular la transicion
            for (String simbolo : afnd.getAlfabeto()) {

                // PASO 2a: Calcular move(subconjunto, simbolo)
                // move(S, a) = { estados alcanzables desde S con el simbolo 'a' }
                Set<String> conjuntoMovimiento = new HashSet<>();
                for (String estado : subconjunto) {
                    // Obtiene las transiciones del estado actual
                    Map<String, Set<String>> transicionesEstado = afnd.getTransiciones().get(estado);
                    // Si existe transicion con este simbolo, agrega todos los destinos
                    if (transicionesEstado != null && transicionesEstado.containsKey(simbolo)) {
                        conjuntoMovimiento.addAll(transicionesEstado.get(simbolo));
                    }
                }

                // PASO 2b: Calcular la clausura epsilon del conjunto movimiento
                // Esto incluye todos los estados alcanzables mediante epsilon
                Set<String> conjuntoClausura = clausuraEpsilon(afnd, conjuntoMovimiento);

                // Si el conjunto no esta vacio, procesamos la transicion
                if (!conjuntoClausura.isEmpty()) {
                    String nombreDestino;

                    // Verifica si ya existe un estado para este subconjunto
                    if (subconjuntoANombre.containsKey(conjuntoClausura)) {
                        // Si ya existe, usa el nombre existente
                        nombreDestino = subconjuntoANombre.get(conjuntoClausura);
                    } else {
                        // Si no existe, crea un nuevo estado
                        nombreDestino = obtenerNombreSubconjunto(conjuntoClausura);
                        subconjuntoANombre.put(conjuntoClausura, nombreDestino);
                        nombreASubconjunto.put(nombreDestino, conjuntoClausura);
                        subconjuntosPendientes.add(conjuntoClausura);  // Lo agrega a pendientes
                    }

                    // Registra la transicion en el AFD
                    transicionesAFD.get(estadoOrigen).put(simbolo, nombreDestino);
                }
            }
        }

        // PASO 3: Construir las transiciones del AFD a partir del mapa
        afd.setEstados(new HashSet<>(transicionesAFD.keySet()));

        for (String estadoOrigen : transicionesAFD.keySet()) {
            for (String simbolo : transicionesAFD.get(estadoOrigen).keySet()) {
                String estadoDestino = transicionesAFD.get(estadoOrigen).get(simbolo);
                afd.agregarTransicion(estadoOrigen, simbolo, estadoDestino);
            }
        }

        // PASO 4: Determinar los estados finales del AFD

        // Un estado del AFD es final si su subconjunto contiene AL MENOS UN
        // estado final del AFND original
        Set<String> estadosFinalesAFD = new HashSet<>();
        for (Map.Entry<Set<String>, String> entrada : subconjuntoANombre.entrySet()) {
            Set<String> subconjunto = entrada.getKey();    // Subconjunto del AFND
            String nombreEstado = entrada.getValue();      // Nombre en el AFD

            // Verifica si este subconjunto contiene algun estado final del AFND
            for (String estadoFinal : afnd.getEstadosFinales()) {
                if (subconjunto.contains(estadoFinal)) {
                    estadosFinalesAFD.add(nombreEstado);
                    break;  // No necesita seguir buscando en este subconjunto
                }
            }
        }
        afd.setEstadosFinales(estadosFinalesAFD);

        // Marca explicitamente como AFD
        afd.setEsAFD(true);

        // PASO 5: Agregar estado TRAMPA para transiciones faltantes

        // Un AFD completo debe tener una transicion para cada (estado, simbolo)
        // Si faltan transiciones, se agrega un estado "trampa" que no es final
        // y que siempre transita hacia si mismo

        boolean necesitaEstadoTrampa = false;
        String nombreEstadoTrampa = "{TRAP}";   // Nombre especial para el estado trampa

        // Verifica cada estado y cada simbolo
        for (String estado : afd.getEstados()) {
            Map<String, Set<String>> transiciones = afd.getTransiciones().getOrDefault(estado, new HashMap<>());
            for (String simbolo : afd.getAlfabeto()) {
                // Si falta una transicion o esta vacia, agrega al estado trampa
                if (!transiciones.containsKey(simbolo) || transiciones.get(simbolo).isEmpty()) {
                    afd.agregarTransicion(estado, simbolo, nombreEstadoTrampa);
                    necesitaEstadoTrampa = true;
                }
            }
        }

        // Si se necesito el estado trampa, lo agregamos al automata
        if (necesitaEstadoTrampa) {
            afd.getEstados().add(nombreEstadoTrampa);           // Agrega el estado trampa
            for (String simbolo : afd.getAlfabeto()) {
                // El estado trampa siempre transita hacia si mismo
                afd.agregarTransicion(nombreEstadoTrampa, simbolo, nombreEstadoTrampa);
            }
            // El estado trampa NO es final (no se agrega a estadosFinalesAFD)
        }

        return afd;    // Retorna el AFD completo
    }

    /**
     * Calcula la clausura epsilon de un conjunto de estados.
     *
     * La clausura epsilon de un conjunto S es el conjunto de todos los estados
     * alcanzables desde S usando cero o mas transiciones epsilon (ε).
     *
     * Algoritmo:
     * 1. Inicializar la clausura con los estados dados
     * 2. Usar una pila para procesar estados
     * 3. Para cada estado, si tiene transiciones epsilon, agregar los destinos
     * 4. Repetir hasta que no se puedan agregar mas estados
     *
     * @param afnd El automata que contiene las transiciones (posiblemente con ε denotado como eps)
     * @param estados El conjunto de estados base
     * @return La clausura epsilon (todos los estados alcanzables via ε)
     */
    private static Set<String> clausuraEpsilon(Automata afnd, Set<String> estados) {
        // La clausura comienza con los estados originales
        Set<String> clausura = new HashSet<>(estados);

        // Usa una pila para el recorrido en profundidad (DFS)
        Stack<String> pila = new Stack<>();
        pila.addAll(estados);

        // Mientras haya estados en la pila por procesar
        while (!pila.isEmpty()) {
            String estado = pila.pop();

            // Obtiene las transiciones de este estado
            Map<String, Set<String>> transicionesEstado = afnd.getTransiciones().get(estado);

            // Si existen transiciones epsilon (eps) desde este estado
            if (transicionesEstado != null && transicionesEstado.containsKey("eps")) {
                // Para cada destino de la transicion epsilon
                for (String siguienteEstado : transicionesEstado.get("eps")) {
                    // Si no esta ya en la clausura, lo agregamos y lo apilamos
                    if (!clausura.contains(siguienteEstado)) {
                        clausura.add(siguienteEstado);
                        pila.push(siguienteEstado);
                    }
                }
            }
        }

        return clausura;
    }

    /**
     * Genera un nombre unico para un subconjunto de estados.
     * El nombre se crea ordenando los estados alfabeticamente y
     * uniendolos con comas dentro de llaves.
     *
     * Ejemplos:
     * - {q0} -> "{q0}"
     * - {q0, q1, q2} -> "{q0,q1,q2}"
     * - {} -> "{}" (conjunto vacio, usualmente estado muerto)
     *
     * @param estados El conjunto de estados a nombrar
     * @return Un string que representa el subconjunto
     */
    private static String obtenerNombreSubconjunto(Set<String> estados) {
        // Convierte el conjunto a una lista para poder ordenarlo
        List<String> estadosOrdenados = new ArrayList<>(estados);
        Collections.sort(estadosOrdenados);   // Orden alfabetico para nombres consistentes

        // Une los estados con comas y envuelve en llaves
        return "{" + String.join(",", estadosOrdenados) + "}";
    }
}
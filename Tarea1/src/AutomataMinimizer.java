import java.util.*;    // Importa colecciones (List, Set, Map, Queue, ArrayList, HashSet, etc.)

/**
 * Clase que minimiza un Autómata Finito Determinista (AFD).
 *
 * La minimización reduce el número de estados de un AFD manteniendo el mismo lenguaje.
 * Utiliza el algoritmo de partición (también conocido como algoritmo de Hopcroft simplificado
 * o algoritmo de llenado de tabla).
 *
 * El algoritmo funciona agrupando estados equivalentes (indistinguibles) y
 * fusionándolos en un solo estado.
 *
 * @authors [Tus Nombres Aquí]
 * @version 1.0
 */
public class AutomataMinimizer {

    /**
     * Minimiza un Autómata Finito Determinista (AFD).
     *
     * El proceso de minimización consta de los siguientes pasos:
     * 1. Eliminar estados inalcanzables (desde el estado inicial)
     * 2. Separar estados finales de no finales (partición inicial)
     * 3. Refinar particiones iterativamente hasta que no cambien
     * 4. Construir un nuevo AFD con los grupos de estados equivalentes
     *
     * @param afd El autómata determinista a minimizar (debe ser AFD)
     * @return Un nuevo AFD minimizado equivalente al original
     * @throws IllegalArgumentException Si el autómata no es AFD
     */
    public static Automata minimize(Automata afd) {

        // ================================================================
        // VERIFICACIÓN: El autómata debe ser AFD
        // ================================================================
        if (!afd.esAFD()) {
            throw new IllegalArgumentException("El autómata debe ser un AFD para minimizarlo.");
        }

        // ================================================================
        // PASO 1: Eliminar estados inalcanzables
        // ================================================================
        // Los estados que no se pueden alcanzar desde el estado inicial
        // no afectan el lenguaje y pueden ser eliminados
        Set<String> estadosAlcanzables = obtenerEstadosAlcanzables(afd);

        // ================================================================
        // PASO 2: Separación inicial (finales vs no finales)
        // ================================================================
        // La partición inicial separa los estados finales de los no finales
        // ya que un estado final y uno no final NUNCA pueden ser equivalentes

        Set<String> estadosFinales = new HashSet<>();
        Set<String> estadosNoFinales = new HashSet<>();

        // Clasifica cada estado alcanzable según sea final o no
        for (String estado : estadosAlcanzables) {
            if (afd.getEstadosFinales().contains(estado)) {
                estadosFinales.add(estado);      // Estado final
            } else {
                estadosNoFinales.add(estado);    // Estado no final
            }
        }

        // Lista de particiones (grupos de estados equivalentes)
        List<Set<String>> particiones = new ArrayList<>();

        // Agrega grupos no vacíos a la lista de particiones
        if (!estadosFinales.isEmpty()) particiones.add(estadosFinales);
        if (!estadosNoFinales.isEmpty()) particiones.add(estadosNoFinales);

        // ================================================================
        // PASO 3: Refinar particiones iterativamente
        // ================================================================
        // Algoritmo de partición:
        // Repetir hasta que no haya cambios:
        //   Para cada grupo, dividirlo según el comportamiento de las transiciones
        //   Dos estados son equivalentes si para cada símbolo:
        //     - Van al mismo grupo (no necesariamente al mismo estado)
        //     - Ambos son finales o ambos no finales

        boolean huboCambio = true;
        while (huboCambio) {
            huboCambio = false;
            List<Set<String>> nuevasParticiones = new ArrayList<>();

            // Procesa cada grupo de la partición actual
            for (Set<String> grupo : particiones) {
                // Mapa: firma (comportamiento) -> conjunto de estados con esa firma
                // La firma describe a qué grupos van las transiciones para cada símbolo
                Map<String, Set<String>> estadosPorFirma = new HashMap<>();

                // Para cada estado en el grupo, calcula su firma
                for (String estado : grupo) {
                    // Construye la firma del estado
                    // La firma es un string que codifica:
                    //   Para cada símbolo del alfabeto, a qué grupo de destino va
                    StringBuilder firma = new StringBuilder();

                    for (String simbolo : afd.getAlfabeto()) {
                        // Obtiene el estado destino para este símbolo
                        String destino = obtenerEstadoDestino(afd, estado, simbolo);

                        // Encuentra a qué grupo pertenece el estado destino
                        int indiceGrupo = encontrarIndiceGrupo(particiones, destino);

                        // Agrega la información a la firma
                        firma.append(simbolo).append("->").append(indiceGrupo).append(";");
                    }

                    // Agrupa los estados que tienen la misma firma
                    String claveFirma = firma.toString();
                    estadosPorFirma.putIfAbsent(claveFirma, new HashSet<>());
                    estadosPorFirma.get(claveFirma).add(estado);
                }

                // Si el grupo se dividió en múltiples subgrupos, hubo cambio
                nuevasParticiones.addAll(estadosPorFirma.values());
                if (estadosPorFirma.size() > 1) {
                    huboCambio = true;    // Se encontraron diferencias entre estados
                }
            }

            // Actualiza las particiones con los nuevos grupos
            particiones = nuevasParticiones;
        }

        // ================================================================
        // PASO 4: Construir el AFD minimizado
        // ================================================================
        // Cada grupo de la partición final se convierte en un estado del AFD minimizado
        return construirAFDMinimizado(afd, particiones, estadosAlcanzables);
    }

    /**
     * Obtiene el conjunto de estados alcanzables desde el estado inicial.
     *
     * Utiliza un recorrido BFS (Búsqueda en Anchura) para explorar todos
     * los estados que se pueden alcanzar desde el estado inicial.
     * Los estados no alcanzables pueden ser eliminados sin afectar el lenguaje.
     *
     * @param afd El autómata determinista
     * @return Conjunto de estados alcanzables
     */
    private static Set<String> obtenerEstadosAlcanzables(Automata afd) {
        Set<String> alcanzables = new HashSet<>();
        Queue<String> cola = new LinkedList<>();

        // Comienza desde el estado inicial
        cola.add(afd.getEstadoInicial());
        alcanzables.add(afd.getEstadoInicial());

        // BFS: mientras haya estados en la cola
        while (!cola.isEmpty()) {
            String estado = cola.poll();

            // Obtiene las transiciones desde este estado
            Map<String, Set<String>> trans = afd.getTransiciones().get(estado);

            if (trans != null) {
                // Para cada símbolo del alfabeto
                for (String simbolo : afd.getAlfabeto()) {
                    Set<String> destinos = trans.get(simbolo);
                    if (destinos != null) {
                        // Para cada estado destino
                        for (String destino : destinos) {
                            if (!alcanzables.contains(destino)) {
                                alcanzables.add(destino);   // Nuevo estado alcanzable
                                cola.add(destino);          // Lo agrega a la cola para explorar
                            }
                        }
                    }
                }
            }
        }

        return alcanzables;
    }

    /**
     * Obtiene el estado destino desde un estado dado con un símbolo específico.
     *
     * En un AFD, para cada (estado, símbolo) solo hay un destino.
     *
     * @param afd El autómata determinista
     * @param estado Estado actual
     * @param simbolo Símbolo de entrada
     * @return Estado destino, o null si no existe transición
     */
    private static String obtenerEstadoDestino(Automata afd, String estado, String simbolo) {
        Map<String, Set<String>> trans = afd.getTransiciones().get(estado);
        if (trans != null && trans.containsKey(simbolo)) {
            // Retorna el primer (y único) elemento del conjunto de destinos
            return trans.get(simbolo).iterator().next();
        }
        return null;
    }

    /**
     * Encuentra el índice del grupo al que pertenece un estado.
     *
     * @param particiones Lista de grupos (conjuntos de estados)
     * @param estado Estado a buscar
     * @return Índice del grupo (0, 1, 2, ...) o -1 si no se encuentra
     */
    private static int encontrarIndiceGrupo(List<Set<String>> particiones, String estado) {
        if (estado == null) return -1;   // Estado nulo (transición indefinida)

        for (int i = 0; i < particiones.size(); i++) {
            if (particiones.get(i).contains(estado)) {
                return i;   // Retorna el índice del grupo
            }
        }
        return -1;   // No encontrado (no debería ocurrir)
    }

    /**
     * Construye un nuevo AFD minimizado a partir de las particiones de equivalencia.
     *
     * Cada grupo de la partición se convierte en un estado del nuevo autómata.
     * El nombre del estado refleja los estados originales que contiene.
     *
     * @param afdOriginal AFD original antes de minimizar
     * @param particiones Grupos de estados equivalentes
     * @param alcanzables Estados alcanzables del autómata original
     * @return AFD minimizado
     */
    private static Automata construirAFDMinimizado(Automata afdOriginal,
                                                   List<Set<String>> particiones,
                                                   Set<String> alcanzables) {
        // Crea un nuevo autómata para el resultado minimizado
        Automata afdMin = new Automata();
        afdMin.setAlfabeto(new HashSet<>(afdOriginal.getAlfabeto()));
        afdMin.setEsAFD(true);   // El resultado siempre es AFD

        // ================================================================
        // Mapeo: estado original -> nombre del grupo en el AFD minimizado
        // ================================================================
        Map<String, String> estadoAGrupo = new HashMap<>();

        // Procesa cada grupo de la partición
        for (Set<String> grupo : particiones) {
            // Genera un nombre para el grupo (ej: "M{q0,q1}" o "M{q2}")
            String nombreGrupo = "M{" + String.join(",", grupo) + "}";

            // Agrega el grupo como un estado del nuevo AFD
            afdMin.getEstados().add(nombreGrupo);

            // Mapea cada estado original a su grupo correspondiente
            for (String estado : grupo) {
                estadoAGrupo.put(estado, nombreGrupo);

                // Si este grupo contiene el estado inicial, ese es el nuevo inicial
                if (estado.equals(afdOriginal.getEstadoInicial())) {
                    afdMin.setEstadoInicial(nombreGrupo);
                }

                // Si el grupo contiene algún estado final, el nuevo estado es final
                if (afdOriginal.getEstadosFinales().contains(estado)) {
                    afdMin.getEstadosFinales().add(nombreGrupo);
                }
            }
        }

        // ================================================================
        // Construir las transiciones del AFD minimizado
        // ================================================================
        // Para cada estado alcanzable del autómata original
        for (String estado : alcanzables) {
            String grupoOrigen = estadoAGrupo.get(estado);

            // Obtiene las transiciones desde este estado
            Map<String, Set<String>> trans = afdOriginal.getTransiciones().get(estado);

            if (trans != null) {
                // Para cada símbolo del alfabeto
                for (String simbolo : afdOriginal.getAlfabeto()) {
                    Set<String> destinos = trans.get(simbolo);

                    // Si existe transición para este símbolo
                    if (destinos != null && !destinos.isEmpty()) {
                        // Obtiene el destino (en AFD solo hay uno)
                        String destino = destinos.iterator().next();

                        // Encuentra a qué grupo pertenece el destino
                        String grupoDestino = estadoAGrupo.get(destino);

                        // Agrega la transición al AFD minimizado
                        afdMin.agregarTransicion(grupoOrigen, simbolo, grupoDestino);
                    }
                }
            }
        }

        return afdMin;
    }
}
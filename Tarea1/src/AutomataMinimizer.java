import java.util.*;

/**
 * Clase que minimiza un Automata Finito Determinista (AFD).
 *
 * La minimizacion reduce el numero de estados de un AFD manteniendo el mismo lenguaje.
 *
 * El algoritmo funciona agrupando estados equivalentes  y
 * fusionandolos en un solo estado.
 *
 */
public class AutomataMinimizer {

    /**
     * Minimiza un Automata Finito Determinista (AFD).
     *
     * El proceso de minimizacion consta de los siguientes pasos:
     * 1. Eliminar estados inalcanzables (desde el estado inicial)
     * 2. Separar estados finales de no finales (particion inicial)
     * 3. Refinar particiones iterativamente hasta que no cambien
     * 4. Construir un nuevo AFD con los grpos de estados equivalentes
     *
     * @param afd El automata determinista a minimizar (debe ser AFD)
     * @return Un nuevo AFD minimizado equivalente al original
     */
    public static Automata minimize(Automata afd) {

        // VERIFICACION: El automata debe ser AFD
        if (!afd.esAFD()) {
            throw new IllegalArgumentException("El automata debe ser un AFD para minimizarlo.");
        }

        // PASO 1: Eliminar estados inalcanzables
        // Los estados que no se pueden alcanzar desde el estado inicial
        // no afectan el lenguaje y pueden ser eliminado
        Set<String> estadosAlcanzables = obtenerEstadosAlcanzables(afd);

        // PASO 2: Separacion inicial (finales vs no finales)
        // La particion inicial separa los estados finales de los no finales
        // ya que un estado final y uno no final NUNCA pueden ser equivalentes

        Set<String> estadosFinales = new HashSet<>();
        Set<String> estadosNoFinales = new HashSet<>();

        // Clasifica cada estado alcanzable segun sea final o no
        for (String estado : estadosAlcanzables) {
            if (afd.getEstadosFinales().contains(estado)) {
                estadosFinales.add(estado);      // Estado final
            } else {
                estadosNoFinales.add(estado);    // Estado no final
            }
        }

        // Lista de particiones (grupos de estados equivalentes)
        List<Set<String>> particiones = new ArrayList<>();

        // Agrega grupos no vacios a la lista de particiones
        if (!estadosFinales.isEmpty()) particiones.add(estadosFinales);
        if (!estadosNoFinales.isEmpty()) particiones.add(estadosNoFinales);

        // PASO 3: perfeccionar particiones iterativamente
        // Algoritmo de particion:
        // Repetir hasta que no haya cambios:
        //   Para cada grupo, dividirlo segun el comportamiento de las transiciones
        //   Dos estados son equivalentes si para cada simbolo:
        //     - Van al mismo grupo (no necesariamente al mismo estado)
        //     - Ambos son finales o ambos no finales

        boolean huboCambio = true;
        while (huboCambio) {
            huboCambio = false;
            List<Set<String>> nuevasParticiones = new ArrayList<>();

            // Procesa cada grupo de la particion actual
            for (Set<String> grupo : particiones) {
                // Mapa: firma (comportamiento) -> conjunto de estados con esa firma
                // La firma describe a que grupos van las transiciones para cada simbolo
                Map<String, Set<String>> estadosPorFirma = new HashMap<>();

                // Para cada estado en el grupo, calcula su firma
                for (String estado : grupo) {
                    // Construye la firma del estado
                    // La firma es un string que codifica:
                    //   Para cada simbolo del alfabeto, a que grupo de destino va
                    StringBuilder firma = new StringBuilder();

                    for (String simbolo : afd.getAlfabeto()) {
                        // Obtiene el estado destino para este simbolo
                        String destino = obtenerEstadoDestino(afd, estado, simbolo);

                        // Encuentra a que grupo pertenece el estado destino
                        int indiceGrupo = encontrarIndiceGrupo(particiones, destino);

                        // Agrega la informacion a la firma
                        firma.append(simbolo).append("->").append(indiceGrupo).append(";");
                    }

                    // Agrupa los estados que tienen la misma firma
                    String claveFirma = firma.toString();
                    estadosPorFirma.putIfAbsent(claveFirma, new HashSet<>());
                    estadosPorFirma.get(claveFirma).add(estado);
                }

                // Si el grupo se dividio en multiples subgrupos, hay cambios
                nuevasParticiones.addAll(estadosPorFirma.values());
                if (estadosPorFirma.size() > 1) {
                    huboCambio = true;    
                }
            }

            // Actualiza las particiones con los nuevos grupos
            particiones = nuevasParticiones;
        }

        // PASO 4: Construir el AFD minimizado
        // Cada grupo de la particion final se convierte en un estado del AFD minimizado
        return construirAFDMinimizado(afd, particiones, estadosAlcanzables);
    }

    /**
     * Obtiene el conjunto de estados alcanzables desde el estado inicial.
     *
     * Utiliza un recorrido BFS  para recorrer todos
     * los estados que se pueden alcanzar desde el estado inicial.
     * Los estados no alcanzables pueden ser eliminados sin afectar el lenguaje.
     *
     */
    private static Set<String> obtenerEstadosAlcanzables(Automata afd) {
        Set<String> alcanzables = new HashSet<>();
        Queue<String> cola = new LinkedList<>();

        cola.add(afd.getEstadoInicial());
        alcanzables.add(afd.getEstadoInicial());

        // BFS: mientras haya estados en la cola
        while (!cola.isEmpty()) {
            String estado = cola.poll();

            // Obtiene las transiciones desde este estado
            Map<String, Set<String>> trans = afd.getTransiciones().get(estado);

            if (trans != null) {
                // Para cada simbolo del alfabeto
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
     * Obtiene el estado destino desde un estado dado con un simbolo especifico.
     *
     * En un AFD, para cada (estado, simbolo) solo hay un destino.
     *
     */
    private static String obtenerEstadoDestino(Automata afd, String estado, String simbolo) {
        Map<String, Set<String>> trans = afd.getTransiciones().get(estado);
        if (trans != null && trans.containsKey(simbolo)) {
            // Retorna el primer (y unico) elemento del conjunto de destinos
            return trans.get(simbolo).iterator().next();
        }
        return null;
    }

    /**
     * Encuentra el indice del grupo al que pertenece un estado.
     *
     * @param particiones Lista de grupos (conjuntos de estados)
     * @param estado Estado a buscar
     * @return Indice del grupo (0, 1, 2, ...) o -1 si no se encuentra
     */
    private static int encontrarIndiceGrupo(List<Set<String>> particiones, String estado) {
        if (estado == null) return -1;   // Estado nulo (transicion indefinida)

        for (int i = 0; i < particiones.size(); i++) {
            if (particiones.get(i).contains(estado)) {
                return i;   // Retorna el indice del grupo
            }
        }
        return -1;   // No encontrado (no deberia ocurrir)
    }

    /**
     * Construye un nuevo AFD minimizado a partir de las particiones de equivalencia.
     *
     * Cada grupo de la particion se convierte en un estado del nuevo automata.
     * El nombre del estado refleja los estados originales que contiene.
     *
     */
    private static Automata construirAFDMinimizado(Automata afdOriginal,
                                                   List<Set<String>> particiones,
                                                   Set<String> alcanzables) {
        // Crea un nuevo automata para el resultado minimizado
        Automata afdMin = new Automata();
        afdMin.setAlfabeto(new HashSet<>(afdOriginal.getAlfabeto()));
        afdMin.setEsAFD(true);   // El resultado siempre es AFD

        // ================================================================
        // Mapeo: estado original -> nombre del grupo en el AFD minimizado
        // ================================================================
        Map<String, String> estadoAGrupo = new HashMap<>();

        // Procesa cada grupo de la particion
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

                // Si el grupo contiene algun estado final, el nuevo estado es final
                if (afdOriginal.getEstadosFinales().contains(estado)) {
                    afdMin.getEstadosFinales().add(nombreGrupo);
                }
            }
        }

        // Construir las transiciones del AFD mininizado
        // Para cada estado alcanzable del automata original
        for (String estado : alcanzables) {
            String grupoOrigen = estadoAGrupo.get(estado);

            // Obtiene las transiciones desde este estado
            Map<String, Set<String>> trans = afdOriginal.getTransiciones().get(estado);

            if (trans != null) {
                // Para cada simbolo del alfabeto
                for (String simbolo : afdOriginal.getAlfabeto()) {
                    Set<String> destinos = trans.get(simbolo);

                    // Si existe transicion para este simbolo
                    if (destinos != null && !destinos.isEmpty()) {
                        // Obtiene el destino (en AFD solo hay uno)
                        String destino = destinos.iterator().next();

                        // Encuentra a que grupo pertenece el destino
                        String grupoDestino = estadoAGrupo.get(destino);

                        // Agrega la transicion al AFD minimizado
                        afdMin.agregarTransicion(grupoOrigen, simbolo, grupoDestino);
                    }
                }
            }
        }

        return afdMin;
    }
}

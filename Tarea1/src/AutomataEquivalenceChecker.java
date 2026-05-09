import java.util.*;

/**
 * Clase que verifica si dos Automatas Finitos Deterministas (AFD) son equivalentes.
 *
 * Dos automatas son equivalentes si aceptan EXACTAMENTE el mismo lenguaje,
 * es decir, para cualquier cadena de entrada, ambos automatas producen el mismo
 * resultado (ambos aceptan o ambos rechazan).
 *
 * El algoritmo utiliza un recorrido BFS (Busqueda en Anchura) sobre pares de estados,
 * verificando que las condiciones de finalizacion coincidan en todo momento.
 *
 */
public class AutomataEquivalenceChecker {

    /**
     * Verifica si dos automatas minimizados son equivalentes.
     *
     * IMPORTANTE: Este metodo asume que ambos automatas ya estan minimizados.
     *
     * Algoritmo:
     * 1. Verificar que los alfabetos sean iguales
     * 2. Crear un par con los estados iniciales de ambos automatas
     * 3. Realizar BFS explorando todos los pares de estados alcanzables
     * 4. Para cada par verificar que ambos sean finales o ambos no finales
     * 5. Si en algun momento las condiciones de finalizacion difieren, NO son equivalentes
     * 6. Si se exploran todos los pares sin encontrar diferencias, son EQUIVALENTES
     *
     * @param min1 Primer automata minimizado (debe ser AFD)
     * @param min2 Segundo automata minimizado (debe ser AFD)
     * @return true si los automatas son equivalentes, false en caso contrario
     */
    public static boolean areEquivalent(Automata min1, Automata min2) {

        // VERIFICACION 1: Los alfabetos deben ser iguales
        // Si los alfabetos son diferentes, los automatas no pueden ser equivalentes
        // porque operan sobre conjuntos de simbolos distintos
        if (!min1.getAlfabeto().equals(min2.getAlfabeto())) {
            return false;   // Alfabetos diferentes -> lenguajes diferentes
        }

        // ESTRUCTURAS DE DATOS PARA EL BFS
        // Cola para el recorrido BFS (Breadth-First Search)
        Queue<ParEstados> cola = new LinkedList<>();

        // Conjunto de pares ya visitados (para evitar ciclos infinitos)
        Set<ParEstados> visitados = new HashSet<>();

        // PASO 1: Comenzar con el par de estados iniciales
        ParEstados parInicial = new ParEstados(min1.getEstadoInicial(), min2.getEstadoInicial());
        cola.add(parInicial);           // Agrega a la cola para procesar
        visitados.add(parInicial);      // Marca como visitado

        // PASO 2: Recorrer todos los pares de estados alcanzables
        while (!cola.isEmpty()) {
            // Toma el siguiente par de la cola (FIFO)
            ParEstados actual = cola.poll();

            // Obtiene los estados individuales de cada automata
            String estado1 = actual.estado1;
            String estado2 = actual.estado2;

            // VERIFICACION 2: Condiciones de finalizacion deben coincidir
            // Verifica si el estado1 es final en el primer automata
            boolean esFinal1 = min1.getEstadosFinales().contains(estado1);

            // Verifica si el estado2 es final en el segundo automata
            boolean esFinal2 = min2.getEstadosFinales().contains(estado2);

            // Si uno es final y el otro no, los automatas NO son equivalentes
            // Porque existe al menos una cadena que lleva a un estado final en uno
            // pero no en el otro
            if (esFinal1 != esFinal2) {
                return false;   // Diferencia encontrada -> no equivalentes
            }

            // VERIFICACION 3: Explorar transiciones para cada simbolo
            // Para cada simbolo del alfabeto (ambos tienen el mismo alfabeto)
            for (String simbolo : min1.getAlfabeto()) {
                // Obtiene el siguiente estado en el primer automata
                String siguiente1 = obtenerSiguienteEstado(min1, estado1, simbolo);

                // Obtiene el siguiente estado en el segundo automata
                String siguiente2 = obtenerSiguienteEstado(min2, estado2, simbolo);

                // VERIFICACION 4: Las transiciones deben estar definidas igual
                // Caso 1: Uno tiene transicion y el otro no
                if ((siguiente1 == null && siguiente2 != null) ||
                        (siguiente1 != null && siguiente2 == null)) {
                    return false;   // Transiciones inconsistentes
                }

                // Caso 2: Ambos tienen transicion (a estados definidos)
                if (siguiente1 != null && siguiente2 != null) {
                    // Crea un par con los siguientes estados
                    ParEstados siguientePar = new ParEstados(siguiente1, siguiente2);

                    // Si este par no ha sido visitado aun
                    if (!visitados.contains(siguientePar)) {
                        visitados.add(siguientePar);    // Marca como visitado
                        cola.add(siguientePar);         // Agrega a la cola para procesar
                    }
                }
                // Si ambos son null, simplemente continuamos (ambos tienen transicion indefinida)
            }
        }

        // Si llegamos aqui, todos los pares visitados cumplen las condiciones
        // No se encontraron diferencias -> los automatas son equivalentes
        return true;
    }

    /**
     * Obtiene el siguiente estado a partir de un estado actual y un simbolo.
     *
     * En un AFD, para cada (estado, simbolo) hay como maximo UNA transicion.
     * Este metodo extrae ese unico estado destino del conjunto de destinos.
     *
     * @param afd El automata determinista (AFD)
     * @param estado Estado actual
     * @param simbolo Simbolo de entrada
     * @return El nombre del estado destino, o null si no existe transicion
     */
    private static String obtenerSiguienteEstado(Automata afd, String estado, String simbolo) {
        // Obtiene el mapa de transiciones para el estado actual
        // La estructura es: transiciones[estado][simbolo] = conjunto de destinos
        Map<String, Set<String>> trans = afd.getTransiciones().get(estado);

        // Verifica si existe transicion para este simbolo
        if (trans != null && trans.containsKey(simbolo)) {
            // Obtiene el conjunto de estados destino (en AFD solo debe haber 1)
            Set<String> destinos = trans.get(simbolo);

            // Si el conjunto no es nulo y no esta vacio, retorna el primer (y unico) destino
            if (destinos != null && !destinos.isEmpty()) {
                // iterator().next() obtiene el primer (y unico) elemento del conjunto
                return destinos.iterator().next();
            }
        }

        // No existe transicion definida para (estado, simbolo)
        return null;
    }

    /**
     * Clase auxiliar interna que representa un par de estados (uno de cada automata).
     *
     * Esta clase es necesaria para:
     * - Almacenar pares en la cola del BFS
     * - Usar conjuntos (HashSet) para evitar procesar el mismo par multiples veces
     * - Implementar equals() y hashCode() para comparacion correcta
     *
     * Ejemplo: Un par (q1, p2) significa que estamos en el estado q1 del primer
     * automata y en el estado p2 del segundo automata.
     */
    private static class ParEstados {
        String estado1;   // Estado del primer automata
        String estado2;   // Estado del segundo automata

        /**
         * Constructor del par de estados.
         * @param estado1 Estado del primer automata
         * @param estado2 Estado del segundo automata
         */
        public ParEstados(String estado1, String estado2) {
            this.estado1 = estado1;
            this.estado2 = estado2;
        }

        /**
         * Compara este par con otro objeto para determinar igualdad.
         * Dos pares son iguales si ambos estados coinciden.
         *
         * @param o Objeto a comparar
         * @return true si son iguales, false en caso contrario
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;                    // Mismo objeto
            if (o == null || getClass() != o.getClass()) return false;  // null o clase diferente
            ParEstados par = (ParEstados) o;               // Convierte a ParEstados

            // Compara ambos estados usando Objects.equals() (maneja nulls)
            return Objects.equals(estado1, par.estado1) &&
                    Objects.equals(estado2, par.estado2);
        }

        /**
         * Calcula el codigo hash del par.
         * Necesario para usar el par como clave en HashSet/HashMap.
         *
         * @return Codigo hash basado en ambos estados
         */
        @Override
        public int hashCode() {
            return Objects.hash(estado1, estado2);
        }
    }
}
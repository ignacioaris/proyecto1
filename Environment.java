import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;


/**
 * La clase Environment almacena variables enteras y cadenas.
 * Almacena funciones definidas por el usuario
 *
 */
public class Environment {
    // Variables globales (enteras y strings)
    private HashMap<String, Integer> globalVariablesInt;
    private HashMap<String, String> globalVariablesStr;

    // Funciones definidas (cuerpo y parámetros)
    private HashMap<String, List<Token>> functions;
    private HashMap<String, List<Token>> functionParameters;

    // Pila de ámbitos para recursión (cada ámbito es un mapa de variables)
    private Stack<HashMap<String, Integer>> localScopes;
    private Stack<HashMap<String, String>> localStrScopes;

    // Contador de profundidad de recursión
    private int recursionDepth;
    private static final int MAX_RECURSION_DEPTH = 1000;

    /**
     *  Constructor del objeto environment
     *  no recive ningun parametro
     */
    public Environment() {
        globalVariablesInt = new HashMap<>();
        globalVariablesStr = new HashMap<>();
        functions = new HashMap<>();
        functionParameters = new HashMap<>();
        localScopes = new Stack<>();
        localStrScopes = new Stack<>();
        recursionDepth = 0;
    }


    public void pushScope() {
        localScopes.push(new HashMap<>());
        localStrScopes.push(new HashMap<>());
    }

    public void popScope() {
        if (!localScopes.isEmpty()) {
            localScopes.pop();
            localStrScopes.pop();
        }
    }

    /**
     * metodo para definir una variable entera y almacenarla
     * @param name nombre de la variable
     * @param value valor numerico de la variable
     *
     */
    public void setVariable(String name, int value) {
        if (!localScopes.isEmpty()) {
            localScopes.peek().put(name, value);
        } else {
            globalVariablesInt.put(name, value);
        }
    }

    /**
     * metodo para definir una variable de cadena y almacenarla
     * @param name nombre de la variable
     * @param value cadena asignada a la variable
     *
     */
    public void setVariable(String name, String value) {
        if (!localStrScopes.isEmpty()) {
            localStrScopes.peek().put(name, value);
        } else {
            globalVariablesStr.put(name, value);
        }
    }

    /**
     * metodo para llamar una variable entera
     * @param name nombre de la variable
     * @return
     */
    public Integer getVariableInt(String name) {
        // Buscar en ámbitos locales (de más reciente a más antiguo)
        for (int i = localScopes.size() - 1; i >= 0; i--) {
            if (localScopes.get(i).containsKey(name)) {
                return localScopes.get(i).get(name);
            }
        }
        // Buscar en variables globales
        return globalVariablesInt.getOrDefault(name, 0);
    }

    /**
     * metodo para llamar una variable entera
     * @param name nombre de la variable
     * @return
     */
    public String getVariableStr(String name) {
        // Buscar en ámbitos locales
        for (int i = localStrScopes.size() - 1; i >= 0; i--) {
            if (localStrScopes.get(i).containsKey(name)) {
                return localStrScopes.get(i).get(name);
            }
        }
        // Buscar en variables globales
        return globalVariablesStr.getOrDefault(name, "");
    }

    /**
     * metodo para declarar una funcion
     * @param name nombre de la funcion
     * @param parameters parametros que recive la funcion
     * @param body cuerpo de la funcion
     *
     */
    public void defineFunction(String name, List<Token> parameters, List<Token> body) {
        // Hacer una copia profunda del cuerpo para evitar problemas de referencia
        List<Token> bodyCopy = new ArrayList<>(body);
        functions.put(name, bodyCopy);
        functionParameters.put(name, new ArrayList<>(parameters));
    }

    /**
     * metodo para llamar una funcion
     * @param name nombre de la funcion
     *
     *
     */
    public List<Token> getFunction(String name) {
        return functions.get(name);
    }

    public List<Token> getFunctionParameters(String name) {
        return functionParameters.get(name);
    }

    // ==================== CONTROL DE RECURSIÓN ====================
    public void enterRecursion() {
        recursionDepth++;
        if (recursionDepth > MAX_RECURSION_DEPTH) {
            throw new RuntimeException("Error: profundidad de recursión excedida (>" + MAX_RECURSION_DEPTH + ")");
        }
    }

    public void exitRecursion() {
        recursionDepth--;
    }

    // ==================== MÉTODOS AUXILIARES ====================
    public boolean isFunctionDefined(String name) {
        return functions.containsKey(name);
    }

    public void clear() {
        globalVariablesInt.clear();
        globalVariablesStr.clear();
        localScopes.clear();
        localStrScopes.clear();
    }

    public void pushFunctionScope() {
        if (localScopes.isEmpty()) {
            localScopes.push(new HashMap<>());
            localStrScopes.push(new HashMap<>());
        } else {
            // Copiar el ámbito anterior para mantener las variables visibles
            localScopes.push(new HashMap<>(localScopes.peek()));
            localStrScopes.push(new HashMap<>(localStrScopes.peek()));
        }
    }
}

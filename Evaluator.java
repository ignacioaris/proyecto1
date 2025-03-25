import java.util.List;
import java.util.ArrayList;

/**
 * Clase encargada de evaluar expresiones LISP representadas como tokens.
 * Proporciona métodos para evaluar diferentes tipos de operaciones y estructuras de control.
 */
public class Evaluator {
    private Environment env;
    private Parser parser;

    /**
     * Constructor que inicializa el evaluador con un entorno específico.
     * @param env El entorno que contiene variables y funciones definidas.
     */
    public Evaluator(Environment env) {
        this.env = env;
        this.parser = new Parser();
    }

    /**
     * Evalúa una lista de tokens que representan una expresión LISP.
     * @param tokens Lista de tokens a evaluar.
     * @return El resultado de la evaluación como una cadena, o un mensaje de error.
     */
    public String evaluate(List<Token> tokens) {
        if (tokens.isEmpty()) {
            return "Error: expresión vacía";
        }

        Token firstToken = tokens.get(0);
        if (!firstToken.getType().equals("PARENTHESIS") || !firstToken.getValue().equals("(")) {
            return "Error: la expresión debe comenzar con '('";
        }

        return evaluateExpression(tokens, 1);
    }

    /**
     * Evalúa una expresión LISP a partir de un índice específico en la lista de tokens.
     * @param tokens Lista de tokens que representan la expresión.
     * @param startIndex Índice desde donde comenzar la evaluación.
     * @return El resultado de la evaluación como una cadena, o un mensaje de error.
     */
    private String evaluateExpression(List<Token> tokens, int startIndex) {
        if (startIndex >= tokens.size()) {
            return "Error: expresión incompleta";
        }

        Token currentToken = tokens.get(startIndex);

        if (currentToken.getType().equals("SYMBOL")) {
            String command = currentToken.getValue();

            switch (command) {
                case "+":
                    return evaluateSum(tokens, startIndex + 1);
                case "-":
                    return evaluateRest(tokens, startIndex + 1);
                case "*":
                    return evaluateMult(tokens, startIndex + 1);
                case "/":
                    return evaluateDiv(tokens, startIndex + 1);
                case "^":
                    return evaluatePow(tokens, startIndex + 1);
                case "%":
                    return evaluateMod(tokens, startIndex + 1);
                case "setq":
                    return evaluateSetq(tokens, startIndex + 1);
                case "print":
                    return evaluatePrint(tokens, startIndex + 1);
                case "defun":
                    return evaluateDefun(tokens, startIndex + 1);
                case "if":
                    return evaluateIf(tokens, startIndex + 1);
                case "while":
                    return evaluateWhile(tokens, startIndex + 1);
                case "for":
                    return evaluateFor(tokens, startIndex + 1);
                case ">":
                    return evaluateLessThan(tokens, startIndex + 1);
                case "<":
                    return evaluateGreaterThan(tokens, startIndex + 1);
                default:
                    // Verificar si es una función definida por el usuario
                    List<Token> functionBody = env.getFunction(command);
                    List<Token> functionParameters = env.getFunctionParameters(command);
                    if (functionBody != null && functionParameters != null) {
                        return evaluateFunctionCall(tokens, startIndex, functionParameters, functionBody);
                    }
                    // Verificar si es una variable
                    Integer intValue = env.getVariableInt(command);
                    if (intValue != null) {
                        return String.valueOf(intValue);
                    }
                    String strValue = env.getVariableStr(command);
                    if (strValue != null && !strValue.isEmpty()) {
                        return strValue;
                    }
                    return "Error: comando no reconocido - " + command;
            }
        }
        else if (currentToken.getType().equals("NUMBER")) {
            return currentToken.getValue();
        }
        else if (currentToken.getType().equals("PARENTHESIS")) {
            if (currentToken.getValue().equals("(")) {
                // Manejar expresiones anidadas
                int endIndex = findMatchingParenthesis(tokens, startIndex);
                if (endIndex == -1) {
                    return "Error: paréntesis no balanceados";
                }
                List<Token> nestedTokens = tokens.subList(startIndex, endIndex + 1);
                return evaluate(nestedTokens);
            } else if (currentToken.getValue().equals(")")) {
                return "Error: paréntesis de cierre inesperado";
            }
        }

        return "Error: token no válido - " + currentToken.getValue();
    }

    private String evaluateFunctionCall(List<Token> tokens, int startIndex,
                                        List<Token> parameters, List<Token> body) {
        // Control de profundidad
        env.enterRecursion();

        // Crear nuevo ámbito para esta llamada
        env.pushFunctionScope();

        try {
            // Evaluar argumentos
            List<String> args = new ArrayList<>();
            int i = startIndex + 1;

            while (i < tokens.size() && !tokens.get(i).getValue().equals(")")) {
                if (tokens.get(i).getValue().equals("(")) {
                    int end = findMatchingParenthesis(tokens, i);
                    List<Token> nested = tokens.subList(i, end + 1);
                    args.add(evaluate(nested));
                    i = end + 1;
                } else {
                    args.add(evaluateToken(tokens, i));
                    i++;
                }
            }

            // Verificar argumentos
            if (args.size() != parameters.size()) {
                return "Error: argumentos incorrectos para " + tokens.get(startIndex).getValue();
            }

            // Asignar parámetros
            for (int j = 0; j < parameters.size(); j++) {
                String paramName = parameters.get(j).getValue();
                String argValue = args.get(j);

                try {
                    int val = Integer.parseInt(argValue);
                    env.setVariable(paramName, val);
                } catch (NumberFormatException e) {
                    env.setVariable(paramName, argValue);
                }
            }

            // Evaluar cuerpo
            return evaluate(body);
        } finally {
            env.popScope();
            env.exitRecursion();
        }
    }

    private String evaluateExpressionWithResult(List<Token> tokens, int startIndex, String previousResult) {
        if (startIndex >= tokens.size()) {
            return previousResult;
        }

        Token currentToken = tokens.get(startIndex);
        if (currentToken.getType().equals("PARENTHESIS") && currentToken.getValue().equals(")")) {
            return previousResult;
        }

        // Si el token actual es un número o un símbolo, continuar evaluando
        if (currentToken.getType().equals("NUMBER") || currentToken.getType().equals("SYMBOL")) {
            String nextResult = evaluateToken(tokens, startIndex);
            if (nextResult.startsWith("Error")) {
                return nextResult;
            }
            // Aquí puedes decidir cómo combinar previousResult y nextResult
            // Por ejemplo, para la suma:
            int result = Integer.parseInt(previousResult) + Integer.parseInt(nextResult);
            return String.valueOf(result);
        }

        // Si el token actual es una expresión anidada, evaluarla y continuar
        if (currentToken.getType().equals("PARENTHESIS") && currentToken.getValue().equals("(")) {
            int endIndex = findMatchingParenthesis(tokens, startIndex);
            if (endIndex == -1) {
                return "Error: paréntesis no balanceados";
            }
            List<Token> nestedTokens = tokens.subList(startIndex, endIndex + 1);
            String nestedResult = evaluate(nestedTokens);
            if (nestedResult.startsWith("Error")) {
                return nestedResult;
            }
            // Aquí puedes decidir cómo combinar previousResult y nestedResult
            // Por ejemplo, para la suma:
            int result = Integer.parseInt(previousResult) + Integer.parseInt(nestedResult);
            return evaluateExpressionWithResult(tokens, endIndex + 1, String.valueOf(result));
        }

        return "Error: token no válido";
    }

    private int findMatchingParenthesis(List<Token> tokens, int startIndex) {
        int parenthesisCount = 1;
        for (int i = startIndex + 1; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.getType().equals("PARENTHESIS")) {
                if (token.getValue().equals("(")) {
                    parenthesisCount++;
                } else if (token.getValue().equals(")")) {
                    parenthesisCount--;
                    if (parenthesisCount == 0) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private String evaluateSum(List<Token> tokens, int startIndex) {
        if (tokens.size() < startIndex + 1) {
            return "Error: suma requiere al menos un operando";
        }

        int result = 0;
        int i = startIndex;

        while (i < tokens.size() && !tokens.get(i).getValue().equals(")")) {
            String currentResult = evaluateToken(tokens, i);
            if (currentResult.startsWith("Error")) {
                return currentResult;
            }
            try {
                result += Integer.parseInt(currentResult);
            } catch (NumberFormatException e) {
                return "Error: operando no numérico en suma";
            }

            // Avanzar al siguiente token, saltando cualquier expresión ya procesada
            if (tokens.get(i).getType().equals("PARENTHESIS") && tokens.get(i).getValue().equals("(")) {
                i = findMatchingParenthesis(tokens, i) + 1;
            } else {
                i++;
            }
        }

        return String.valueOf(result);
    }

    private String evaluateRest(List<Token> tokens, int startIndex) {
        if (tokens.size() < startIndex + 1) {
            return "Error: resta requiere al menos un operando";
        }

        String firstResult = evaluateToken(tokens, startIndex);
        if (firstResult.startsWith("Error")) {
            return firstResult;
        }
        int result = Integer.parseInt(firstResult);

        for (int i = startIndex + 1; i < tokens.size(); i++) {
            String nestedResult = evaluateToken(tokens, i);
            if (nestedResult.startsWith("Error")) {
                return nestedResult;
            }
            result -= Integer.parseInt(nestedResult);
            i = skipProcessedTokens(tokens, i);
        }
        return String.valueOf(result);
    }

    private String evaluateMult(List<Token> tokens, int startIndex) {
        if (tokens.size() < startIndex + 1) {
            return "Error: multiplicación requiere al menos un operando";
        }

        // Evaluar el primer operando
        String firstResult = evaluateToken(tokens, startIndex);
        if (firstResult.startsWith("Error")) {
            return firstResult;
        }
        int result = Integer.parseInt(firstResult);

        // Evaluar los operandos restantes
        for (int i = startIndex + 1; i < tokens.size(); i++) {
            String nestedResult = evaluateToken(tokens, i);
            if (nestedResult.startsWith("Error")) {
                return nestedResult;
            }
            result *= Integer.parseInt(nestedResult);
            i = skipProcessedTokens(tokens, i);
        }
        return String.valueOf(result);
    }

    private String evaluateDiv(List<Token> tokens, int startIndex) {
        String firstResult = evaluateToken(tokens, startIndex);
        if (firstResult.startsWith("Error")) {
            return firstResult;
        }
        double result = Double.parseDouble(firstResult);

        for (int i = startIndex + 1; i < tokens.size(); i++) {
            String nestedResult = evaluateToken(tokens, i);
            if (nestedResult.startsWith("Error")) {
                return nestedResult;
            }
            double operand = Double.parseDouble(nestedResult);
            if (operand == 0) {
                return "Error: división por cero";
            }
            result /= operand;
            i = skipProcessedTokens(tokens, i);
        }
        return String.valueOf(result);
    }

    private String evaluatePow(List<Token> tokens, int startIndex) {
        String firstResult = evaluateToken(tokens, startIndex);
        if (firstResult.startsWith("Error")) {
            return firstResult;
        }
        double result = Double.parseDouble(firstResult);

        for (int i = startIndex + 1; i < tokens.size(); i++) {
            String nestedResult = evaluateToken(tokens, i);
            if (nestedResult.startsWith("Error")) {
                return nestedResult;
            }
            result = Math.pow(result, Double.parseDouble(nestedResult));
            i = skipProcessedTokens(tokens, i);
        }
        return String.valueOf(result);
    }

    private String evaluateMod(List<Token> tokens, int startIndex) {
        String firstResult = evaluateToken(tokens, startIndex);
        if (firstResult.startsWith("Error")) {
            return firstResult;
        }
        int result = Integer.parseInt(firstResult);

        for (int i = startIndex + 1; i < tokens.size(); i++) {
            String nestedResult = evaluateToken(tokens, i);
            if (nestedResult.startsWith("Error")) {
                return nestedResult;
            }
            int operand = Integer.parseInt(nestedResult);
            if (operand == 0) {
                return "Error: módulo por cero";
            }
            result %= operand;
            i = skipProcessedTokens(tokens, i);
        }
        return String.valueOf(result);
    }

    private String evaluateSetq(List<Token> tokens, int startIndex) {
        if (tokens.size() < startIndex + 2) {
            return "Error: setq mal formado";
        }

        String varName = tokens.get(startIndex).getValue();
        String valueResult;

        // Caso 1: Asignación directa de un número o variable existente
        if (tokens.get(startIndex + 1).getType().equals("NUMBER") ||
                (tokens.get(startIndex + 1).getType().equals("SYMBOL") &&
                        (env.getVariableInt(tokens.get(startIndex + 1).getValue()) != null ||
                                env.getVariableStr(tokens.get(startIndex + 1).getValue()) != null))) {

            valueResult = evaluateToken(tokens, startIndex + 1);
        }
        // Caso 2: Expresión compleja entre paréntesis
    else if (tokens.get(startIndex + 1).getType().equals("PARENTHESIS") &&
                tokens.get(startIndex + 1).getValue().equals("(")) {

            int endIndex = findMatchingParenthesis(tokens, startIndex + 1);
            if (endIndex == -1) {
                return "Error: paréntesis no balanceados en setq";
            }
            List<Token> expressionTokens = tokens.subList(startIndex + 1, endIndex + 1);
            valueResult = evaluate(expressionTokens);
        }
        else {
            return "Error: valor de asignación no válido";
        }

        if (valueResult.startsWith("Error")) {
            return valueResult;
        }

        try {
            // Intentar asignar como entero primero
            int intValue = Integer.parseInt(valueResult);
            env.setVariable(varName, intValue);
        } catch (NumberFormatException e) {
            // Si no es número, asignar como string
            env.setVariable(varName, valueResult);
        }

        return "Variable " + varName + " asignada";
    }

    private String evaluatePrint(List<Token> tokens, int startIndex) {
        String result = evaluateToken(tokens, startIndex);
        System.out.println(result);
        return result;
    }

    private int skipProcessedTokens(List<Token> tokens, int index) {
        int endIndex = findMatchingParenthesis(tokens, index);
        return endIndex != -1 ? endIndex : index;
    }

    private String evaluateToken(List<Token> tokens, int index) {
        if (index >= tokens.size()) {
            return "Error: índice fuera de rango";
        }

        Token token = tokens.get(index);

        if (token.getType().equals("NUMBER")) {
            return token.getValue();
        }
        else if (token.getType().equals("SYMBOL")) {
            // Verificar si es una variable numérica
            Integer intValue = env.getVariableInt(token.getValue());
            if (intValue != null) {
                return String.valueOf(intValue);
            }
            // Verificar si es una variable string
            String strValue = env.getVariableStr(token.getValue());
            if (strValue != null && !strValue.isEmpty()) {
                return strValue;
            }
            return "Error: símbolo no reconocido - " + token.getValue();
        }
        else if (token.getType().equals("PARENTHESIS") && token.getValue().equals("(")) {
            // Manejar expresiones anidadas
            int endIndex = findMatchingParenthesis(tokens, index);
            if (endIndex == -1) {
                return "Error: paréntesis no balanceados";
            }
            List<Token> nestedTokens = tokens.subList(index, endIndex + 1);
            return evaluate(nestedTokens);
        }

        return "Error: token no válido";
    }

    private String evaluateDefun(List<Token> tokens, int startIndex) {
        if (tokens.size() < startIndex + 3) {
            return "Error: defun mal formado";
        }

        String functionName = tokens.get(startIndex).getValue();
        List<Token> parameters = new ArrayList<>();
        List<Token> body = new ArrayList<>();

        // Obtener los parámetros
        int i = startIndex + 1;
        if (!tokens.get(i).getType().equals("PARENTHESIS") || !tokens.get(i).getValue().equals("(")) {
            return "Error: los parámetros deben estar entre paréntesis";
        }
        i++;
        while (i < tokens.size() && !(tokens.get(i).getType().equals("PARENTHESIS") && tokens.get(i).getValue().equals(")"))) {
            parameters.add(tokens.get(i));
            i++;
        }
        i++;

        // Obtener el cuerpo de la función
        while (i < tokens.size() && !(tokens.get(i).getType().equals("PARENTHESIS") && tokens.get(i).getValue().equals(")"))) {
            body.add(tokens.get(i));
            i++;
        }

        // Guardar la función en el entorno (parámetros y cuerpo)
        env.defineFunction(functionName, parameters, body);
        return "Función " + functionName + " definida";
    }

    // En Evaluator.java

    private String evaluateIf(List<Token> tokens, int startIndex) {
        // Verificar estructura básica
        if (tokens.size() < startIndex + 3) {
            return "Error: Estructura if incompleta";
        }

        // Evaluar la condición
        String conditionResult = evaluateToken(tokens, startIndex);
        if (conditionResult.startsWith("Error")) {
            return conditionResult;
        }

        // Determinar si la condición es verdadera (1) o falsa (0)
        boolean condition = isTruthy(conditionResult);

        // Encontrar el inicio del bloque then
        int thenStart = startIndex + 1;
        if (thenStart >= tokens.size()) {
            return "Error: falta rama then";
        }

        // Capturar el bloque then completo
        int thenEnd = findMatchingParenthesis(tokens, thenStart);
        if (thenEnd == -1) {
            return "Error: paréntesis no balanceados en rama then";
        }
        List<Token> thenBranch = tokens.subList(thenStart, thenEnd + 1);

        // Buscar rama else (opcional)
        List<Token> elseBranch = null;
        int elseStart = thenEnd + 1;

        if (elseStart < tokens.size() && !tokens.get(elseStart).getValue().equals(")")) {
            int elseEnd = findMatchingParenthesis(tokens, elseStart);
            if (elseEnd == -1) {
                return "Error: paréntesis no balanceados en rama else";
            }
            elseBranch = tokens.subList(elseStart, elseEnd + 1);
        }

        // Ejecutar solo la rama correspondiente
        if (condition) {
            return evaluate(thenBranch);
        } else {
            return elseBranch != null ? evaluate(elseBranch) : "";
        }
    }

    // Método auxiliar para determinar si un valor es "truthy"
    private boolean isTruthy(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        try {
            double num = Double.parseDouble(value);
            return num != 0.0;
        } catch (NumberFormatException e) {
            return true; // Cualquier string no vacío es verdadero
        }
    }

    // Modificar los operadores de comparación para devolver "1" o "0"
    private String evaluateGreaterThan(List<Token> tokens, int startIndex) {
        if (tokens.size() < startIndex + 2) {
            return "Error: > requiere dos operandos";
        }
        String left = evaluateToken(tokens, startIndex);
        String right = evaluateToken(tokens, startIndex + 1);
        if (left.startsWith("Error") || right.startsWith("Error")) {
            return "Error en operación >";
        }
        try {
            return Double.parseDouble(left) > Double.parseDouble(right) ? "1" : "0";
        } catch (NumberFormatException e) {
            return "Error: operandos no numéricos para >";
        }
    }

    private String evaluateWhile(List<Token> tokens, int startIndex) {
        if (tokens.size() < startIndex + 2) {
            return "Error: bucle while mal formado";
        }

        String result = "0"; // Valor por defecto si el bucle no se ejecuta

        while (true) {
            // Evaluar la condición
            String conditionResult = evaluateToken(tokens, startIndex);
            if (conditionResult.startsWith("Error")) {
                return conditionResult;
            }

            boolean condition = !conditionResult.equals("0");
            if (!condition) {
                break;
            }

            // Ejecutar el cuerpo del bucle
            int bodyStart = startIndex + 1;
            int bodyEnd = findMatchingParenthesis(tokens, bodyStart);
            List<Token> body = tokens.subList(bodyStart, bodyEnd + 1);
            result = evaluate(body);
        }

        return result;
    }

    private String evaluateFor(List<Token> tokens, int startIndex) {
        if (tokens.size() < startIndex + 4) {
            return "Error: bucle for mal formado";
        }

        // Inicialización
        String initResult = evaluateToken(tokens, startIndex);
        if (initResult.startsWith("Error")) {
            return initResult;
        }

        String result = "0";

        while (true) {
            // Condición
            String conditionResult = evaluateToken(tokens, startIndex + 1);
            if (conditionResult.startsWith("Error")) {
                return conditionResult;
            }

            boolean condition = !conditionResult.equals("0");
            if (!condition) {
                break;
            }

            // Cuerpo del bucle
            int bodyStart = startIndex + 2;
            int bodyEnd = findMatchingParenthesis(tokens, bodyStart);
            List<Token> body = tokens.subList(bodyStart, bodyEnd + 1);
            result = evaluate(body);

            // Actualización
            String updateResult = evaluateToken(tokens, bodyEnd + 1);
            if (updateResult.startsWith("Error")) {
                return updateResult;
            }
        }

        return result;
    }



    private String evaluateLessThan(List<Token> tokens, int startIndex) {
        if (tokens.size() < startIndex + 2) {
            return "Error: < requiere dos operandos";
        }
        String left = evaluateToken(tokens, startIndex);
        String right = evaluateToken(tokens, startIndex + 1);
        if (left.startsWith("Error") || right.startsWith("Error")) {
            return "Error en operación <";
        }
        try {
            return Double.parseDouble(left) < Double.parseDouble(right) ? "1" : "0";
        } catch (NumberFormatException e) {
            return "Error: operandos no numéricos para <";
        }
    }
}

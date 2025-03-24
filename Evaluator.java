import java.util.List;
import java.util.ArrayList;

public class Evaluator {
    private Environment env;
    private Parser parser;

    public Evaluator(Environment env) {
        this.env = env;
        this.parser = new Parser();
    }

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
                default:
                    // Verificar si es una función definida por el usuario
                    List<Token> functionBody = env.getFunction(command);
                    List<Token> functionParameters = env.getFunctionParameters(command);
                    if (functionBody != null && functionParameters != null) {
                        return evaluateFunctionCall(tokens, startIndex, functionParameters, functionBody);
                    }
                    Integer intValue = env.getVariableInt(command);
                    if (intValue != null) {
                        return String.valueOf(intValue);
                    }
                    String strValue = env.getVariableStr(command);
                    if (strValue != null && !strValue.isEmpty()) {
                        return strValue;
                    }
                    return "Error: comando no reconocido";
            }
        } else if (currentToken.getType().equals("NUMBER")) {
            return currentToken.getValue();
        } else if (currentToken.getType().equals("PARENTHESIS") && currentToken.getValue().equals("(")) {
            // Manejar expresiones anidadas
            int endIndex = findMatchingParenthesis(tokens, startIndex);
            if (endIndex == -1) {
                return "Error: paréntesis no balanceados";
            }
            List<Token> nestedTokens = tokens.subList(startIndex, endIndex + 1);
            String nestedResult = evaluate(nestedTokens);
            return nestedResult; // Devolver el resultado de la expresión anidada
        }

        return "Error: token no válido";
    }

    private String evaluateFunctionCall(List<Token> tokens, int startIndex, List<Token> parameters, List<Token> body) {
        // Obtener los argumentos de la llamada a la función
        List<String> arguments = new ArrayList<>();
        int i = startIndex + 1; // Empezar después del nombre de la función

        while (i < tokens.size() && !(tokens.get(i).getType().equals("PARENTHESIS") && tokens.get(i).getValue().equals(")"))) {
            String argValue = evaluateToken(tokens, i);
            if (argValue.startsWith("Error")) {
                return argValue;
            }
            arguments.add(argValue);
            i++;
        }

        // Verificar que el número de argumentos coincida con el número de parámetros
        if (arguments.size() != parameters.size()) {
            return "Error: número de argumentos no coincide con el número de parámetros";
        }

        // Asignar los valores de los argumentos a los parámetros en el entorno
        for (int j = 0; j < parameters.size(); j++) {
            String paramName = parameters.get(j).getValue();
            int argValue = Integer.parseInt(arguments.get(j));
            env.setVariable(paramName, argValue);
        }

        // Evaluar el cuerpo de la función con los parámetros asignados
        return evaluate(body);
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
            result += Integer.parseInt(nestedResult);
            i = skipProcessedTokens(tokens, i);
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
        String valueResult = evaluateToken(tokens, startIndex + 1);
        if (valueResult.startsWith("Error")) {
            return valueResult;
        }
        env.setVariable(varName, Integer.parseInt(valueResult));
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
        } else if (token.getType().equals("SYMBOL")) {
            Integer intValue = env.getVariableInt(token.getValue());
            if (intValue != null) {
                return String.valueOf(intValue);
            }
            String strValue = env.getVariableStr(token.getValue());
            if (strValue != null && !strValue.isEmpty()) {
                return strValue;
            }
            return "Error: símbolo no reconocido - " + token.getValue();
        } else if (token.getType().equals("PARENTHESIS") && token.getValue().equals("(")) {

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
}

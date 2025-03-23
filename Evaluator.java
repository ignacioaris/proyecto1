import java.util.List;

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
        if (firstToken.getType().equals("SYMBOL")) {
            String command = firstToken.getValue();

            switch (command) {
                case "+":
                    return evaluateSum(tokens);
                case "-":
                    return evaluateRest(tokens);
                case "*":
                    return evaluateMult(tokens);
                case "/":
                    return evaluateDiv(tokens);
                case "^":
                    return evaluatePow(tokens);
                case "%":
                    return evaluateMod(tokens);
                case "setq":
                    return evaluateSetq(tokens);
                case "print":
                    return evaluatePrint(tokens);
                default:
                    if (env.getVariableInt(command) != 0) {
                        return String.valueOf(env.getVariableInt(command));
                    } else if (!env.getVariableStr(command).isEmpty()) {
                        return env.getVariableStr(command);
                    }
                    return "Error: comando no reconocido";
            }
        } else if (firstToken.getType().equals("EXPRESSION")) {
            // Evaluar la expresión anidada
            List<Token> nestedTokens = parser.tokenize(firstToken.getValue());
            return evaluate(nestedTokens);
        }

        return "Error: comando no reconocido";
    }

    private String evaluateSum(List<Token> tokens) {
        int sum = 0;
        for (int i = 1; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            String result = evaluateToken(t);
            if (result.startsWith("Error")) {
                return result; // Propagamos el error
            }
            sum += Integer.parseInt(result);
        }
        return String.valueOf(sum);
    }

    private String evaluateMult(List<Token> tokens) {
        int result = 1;
        for (int i = 1; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            String nestedResult = evaluateToken(t);
            if (nestedResult.startsWith("Error")) {
                return nestedResult; // Propagamos el error
            }
            result *= Integer.parseInt(nestedResult);
        }
        return String.valueOf(result);
    }

    private String evaluateToken(Token token) {
        if (token.getType().equals("NUMBER")) {
            return token.getValue();
        } else if (token.getType().equals("SYMBOL")) {
            int value = env.getVariableInt(token.getValue());
            if (value != 0) {
                return String.valueOf(value);
            } else {
                String strValue = env.getVariableStr(token.getValue());
                if (!strValue.isEmpty()) {
                    return strValue;
                }
            }
        } else if (token.getType().equals("EXPRESSION")) {
            // Evaluar la expresión anidada
            List<Token> nestedTokens = parser.tokenize(token.getValue());
            return evaluate(nestedTokens);
        }
        return "Error: token no válido";
    }

    // Métodos evaluateRest, evaluateDiv, evaluatePow, evaluateMod, evaluateSetq, evaluatePrint
    // deben modificarse de manera similar para manejar errores y expresiones anidadas.

    private String evaluateRest(List<Token> tokens) {
        if (tokens.size() < 2) {
            return "Error: resta requiere al menos un operando";
        }

        // Evaluar el primer operando
        String firstResult = evaluateToken(tokens.get(1));
        if (firstResult.startsWith("Error")) {
            return firstResult; // Propagamos el error
        }
        int result = Integer.parseInt(firstResult);

        // Restar los operandos restantes
        for (int i = 2; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            String nestedResult = evaluateToken(t);
            if (nestedResult.startsWith("Error")) {
                return nestedResult; // Propagamos el error
            }
            result -= Integer.parseInt(nestedResult);
        }
        return String.valueOf(result);
    }

    private String evaluateDiv(List<Token> tokens) {
        if (tokens.size() < 2) {
            return "Error: división requiere al menos un operando";
        }

        // Evaluar el primer operando
        String firstResult = evaluateToken(tokens.get(1));
        if (firstResult.startsWith("Error")) {
            return firstResult; // Propagamos el error
        }
        double result = Double.parseDouble(firstResult);

        // Dividir por los operandos restantes
        for (int i = 2; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            String nestedResult = evaluateToken(t);
            if (nestedResult.startsWith("Error")) {
                return nestedResult; // Propagamos el error
            }
            double operand = Double.parseDouble(nestedResult);
            if (operand == 0) {
                return "Error: división por cero";
            }
            result /= operand;
        }
        return String.valueOf(result);
    }

    private String evaluatePow(List<Token> tokens) {
        if (tokens.size() < 2) {
            return "Error: potencia requiere al menos un operando";
        }

        // Evaluar el primer operando
        String firstResult = evaluateToken(tokens.get(1));
        if (firstResult.startsWith("Error")) {
            return firstResult; // Propagamos el error
        }
        double result = Double.parseDouble(firstResult);

        // Elevar a la potencia de los operandos restantes
        for (int i = 2; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            String nestedResult = evaluateToken(t);
            if (nestedResult.startsWith("Error")) {
                return nestedResult; // Propagamos el error
            }
            result = Math.pow(result, Double.parseDouble(nestedResult));
        }
        return String.valueOf(result);
    }

    private String evaluateMod(List<Token> tokens) {
        if (tokens.size() < 2) {
            return "Error: módulo requiere al menos un operando";
        }

        // Evaluar el primer operando
        String firstResult = evaluateToken(tokens.get(1));
        if (firstResult.startsWith("Error")) {
            return firstResult; // Propagamos el error
        }
        int result = Integer.parseInt(firstResult);

        // Aplicar módulo con los operandos restantes
        for (int i = 2; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            String nestedResult = evaluateToken(t);
            if (nestedResult.startsWith("Error")) {
                return nestedResult; // Propagamos el error
            }
            int operand = Integer.parseInt(nestedResult);
            if (operand == 0) {
                return "Error: módulo por cero";
            }
            result %= operand;
        }
        return String.valueOf(result);
    }

    private String evaluateSetq(List<Token> tokens) {
        if (tokens.size() < 3) {
            return "Error: setq mal formado";
        }

        String varName = tokens.get(1).getValue();
        Token valueToken = tokens.get(2);

        // Evaluar el valor a asignar
        String valueResult = evaluateToken(valueToken);
        if (valueResult.startsWith("Error")) {
            return valueResult; // Propagamos el error
        }

        if (valueToken.getType().equals("NUMBER")) {
            env.setVariable(varName, Integer.parseInt(valueResult));
        } else {
            env.setVariable(varName, valueResult);
        }

        return "Variable " + varName + " asignada";
    }

    private String evaluatePrint(List<Token> tokens) {
        if (tokens.size() < 2) {
            return "Error: print requiere un argumento";
        }

        // Evaluar el argumento a imprimir
        String result = evaluateToken(tokens.get(1));
        if (result.startsWith("Error")) {
            return result; // Propagamos el error
        }

        // Imprimir el resultado
        System.out.println(result);
        return result;
    }
}

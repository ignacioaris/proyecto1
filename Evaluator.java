import java.util.List;

public class Evaluator {
    private Environment env;

    public Evaluator(Environment env) {
        this.env = env;
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
                    return String.valueOf(evaluateSum(tokens));
                case "-":
                    return String.valueOf(evaluateRest(tokens));
                case "*":
                    return String.valueOf(evaluateMult(tokens));
                case "/":
                    return String.valueOf(evaluateDiv(tokens));
                case "setq":
                    return evaluateSetq(tokens);
                default:

            }
        }

        return "Error: comando no reconocido";
    }

    private int evaluateSum(List<Token> tokens) {
        int sum = 0;
        for (int i = 1; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t.getType().equals("NUMBER")) {
                sum += Integer.parseInt(t.getValue());
            }
        }
        return sum;
    }

    private int evaluateRest(List<Token> tokens) {
        Token firstToken = tokens.get(1);
        int result = Integer.parseInt(firstToken.getValue());
        for (int i = 2; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t.getType().equals("NUMBER")) {
                result -= Integer.parseInt(t.getValue());
            }
        }
        return result;
    }

    private int evaluateMult(List<Token> tokens) {
        Token firstToken = tokens.get(1);
        int result = Integer.parseInt(firstToken.getValue());
        for (int i = 2; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t.getType().equals("NUMBER")) {
                result *= Integer.parseInt(t.getValue());
            }
        }
        return result;
    }

    private int evaluateDiv(List<Token> tokens) {
        Token firstToken = tokens.get(1);
        int result = Integer.parseInt(firstToken.getValue());
        for (int i = 2; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t.getType().equals("NUMBER")) {
                result /= Integer.parseInt(t.getValue());
            }
        }
        return result;
    }

    private String evaluateSetq(List<Token> tokens) {
        if (tokens.size() < 3) return "Error: setq mal formado";

        String varName = tokens.get(1).getValue();
        Token valueToken = tokens.get(2);

        if (valueToken.getType().equals("NUMBER")) {
            env.setVariable(varName, Integer.parseInt(valueToken.getValue()));
        } else {
            env.setVariable(varName, valueToken.getValue());
        }

        return "Variable " + varName + " asignada";
    }

}

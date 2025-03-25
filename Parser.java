import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * La clase Parser analiza una cadena de texto que representa una expresion entre parentesis y se convierte en lista de tokens
 *
 * Soporta expresiones anidadas, números y símbolos
 */
public class Parser {


    /**
     * Tokeniza una expresión de código que debe comenzar y terminar con paréntesis
     *
     * @param code la cadena de entrada que contiene la expresión a tokenizar
     * @return una lista de objetos Token que representan la expresión tokenizada
     * @throws RuntimeException si la expresión no comienza y termina con paréntesis
     */
    public List<Token> tokenize(String code) {
        // Validar balance de paréntesis primero
        int balance = 0;
        for (char c : code.toCharArray()) {
            if (c == '(') balance++;
            if (c == ')') balance--;
            if (balance < 0) {
                throw new RuntimeException("Error: paréntesis de cierre sin apertura");
            }
        }
        if (balance != 0) {
            throw new RuntimeException("Error: paréntesis no balanceados");
        }

        // Eliminar la validación que requiere que todo el código esté entre paréntesis
        return tokenizeRecursive(code);
    }

    /**
     * Realiza la tokenización recursiva del contenido interno de una expresion
     *
     * @param code la parte interna de la expresión sin los paréntesis externos
     * @return una lista de tokens que representan números, símbolos o expresiones anidadas
     */
    private List<Token> tokenizeRecursive(String code) {
        List<Token> tokens = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(code, " ()", true);

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            if (!token.isEmpty()) {
                if (token.equals("(") || token.equals(")")) {
                    tokens.add(new Token("PARENTHESIS", token));
                } else if (token.matches("-?\\d+")) { // Soporta valores negativos
                    tokens.add(new Token("NUMBER", token));
                } else {
                    tokens.add(new Token("SYMBOL", token));
                }
            }
        }
        return tokens;
    }
}

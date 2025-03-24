import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * La clase Parser se encarga de analizar una cadena de texto que representa una expresion entre parentesis y se convierte en lista de tokens
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
        List<Token> tokens = new ArrayList<>();

        // Verificar que la expresión comience con '(' y termine con ')'
        if (!code.startsWith("(") || !code.endsWith(")")) {
            throw new RuntimeException("Error: la expresión debe estar entre paréntesis");
        }

        // Eliminar el primer y último paréntesis
        String innerCode = code.substring(1, code.length() - 1).trim();

        // Tokenizar el contenido interno
        tokens = tokenizeRecursive(innerCode);
        return tokens;
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
                if (token.equals("(")) {
                    // Encontrar la expresión anidada
                    String nestedExpr = readNestedExpression(tokenizer);
                    tokens.add(new Token("EXPRESSION", nestedExpr));
                } else if (token.matches("-?\\d+")) {
                    tokens.add(new Token("NUMBER", token));
                } else {
                    tokens.add(new Token("SYMBOL", token));
                }
            }
        }
        return tokens;
    }

    /**
     * Lee una expresión anidada completa desde el tokenizer, incluyendo paréntesis 
     *
     * @param tokenizer el StringTokenizer que contiene los tokens restantes
     * @return una cadena que representa la expresión anidada completa
     */
    private String readNestedExpression(StringTokenizer tokenizer) {
        StringBuilder nestedExpr = new StringBuilder("(");
        int parenthesisCount = 1;

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            nestedExpr.append(token);

            if (token.equals("(")) {
                parenthesisCount++;
            } else if (token.equals(")")) {
                parenthesisCount--;
                if (parenthesisCount == 0) {
                    break;
                }
            }
        }

        return nestedExpr.toString();
    }
}

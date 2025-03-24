import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Parser {

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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Parser {

    public List<Token> tokenize(String code) {
        List<Token> tokens = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(code, " ()", true);

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            if (!token.isEmpty()) {
                if (token.matches("-?\\d+")) {
                    tokens.add(new Token("NUMBER", token));
                } else if (token.equals("(") || token.equals(")")) {
                    tokens.add(new Token("PARENTHESIS", token));
                } else {
                    tokens.add(new Token("SYMBOL", token));
                }
            }
        }
        return tokens;
    }
}

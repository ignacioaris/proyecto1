import java.util.List;
public class Interpreter {
    private Parser parser;
    private Evaluator evaluator;
    private Environment environment;

    public Interpreter() {
        this.environment = new Environment();
        this.parser = new Parser();
        this.evaluator = new Evaluator(environment);
    }

    public void run(String code) {
        try {
            List<Token> tokens = parser.tokenize(code);
            String result = evaluator.evaluate(tokens);
            //System.out.println(result);
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

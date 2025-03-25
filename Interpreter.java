import java.util.List;

/**
 * La clase Interpreter maneja la entrada de codigo y el paso por otras clases
 *
 */
public class Interpreter {
    private Parser parser;
    private Evaluator evaluator;
    private Environment environment;

    public Interpreter() {
        this.environment = new Environment();
        this.parser = new Parser();
        this.evaluator = new Evaluator(environment);
    }

    /**
     * metodo que corre el codigo
     * @param code codigo LISP en forma de cadena
     */
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


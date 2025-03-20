import java.util.HashMap;
import java.util.List;

public class Environment{
  private HashMap<String, Integer> variablesInt;
  private HashMap<String, String> variablesStr;
  private HashMap<String, List<Token>> functions;

public Environment() {
    variablesInt = new HashMap<>();
    variablesStr = new HashMap<>();
    functions = new HashMap<>();
    }
}

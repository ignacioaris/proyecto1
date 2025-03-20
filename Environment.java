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

public void setVariable(String name, int value) {
    variablesInt.put(name, value);
    }

public Integer getVariableInt(String name) {
     return variablesInt.getOrDefault(name, 0);
    }

public void setVariable(String name, String value) {
      variablesStr.put(name, value);
    }

public String getVariableStr(String name) {
      return variablesStr.getOrDefault(name, "");
    }

public void defineFunction(String name, List<Token> body) {
      functions.put(name, body);
    }

public List<Token> getFunction(String name) {
      return functions.get(name);
    }
  
}

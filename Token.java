/**
 * La clase Token representa un componente léxico con un tipo y un valor
 */
public class Token {
    /** El tipo del token (por ejemplo, palabra clave, identificador, operador, etc.). */
    private String type;

    /** El valor del token (el texto real del token). */
    private String value;

    /**
     * Crea un nuevo Token con el tipo y valor especificados.
     *
     * @param type el tipo del token
     * @param value el valor del token
     */
    public Token(String type, String value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Obtiene el tipo del token.
     *
     * @return el tipo del token
     */
    public String getType() {
        return type;
    }

    /**
     * Obtiene el valor del token.
     *
     * @return el valor del token
     */
    public String getValue() {
        return value;
    }
}

package portal.errors;

public class NoRightsException extends Exception {
    public NoRightsException(String message) {
        super(message);
    }
    
    public NoRightsException() {
        super("You have no rights to make this action!");
    }
}
package portal.errors;

public class NoFreeSeatsException extends Exception {
    public NoFreeSeatsException(String message) {
        super(message);
    }
    
    public NoFreeSeatsException() {
        super("In educational institution no free seats!");
    }
}
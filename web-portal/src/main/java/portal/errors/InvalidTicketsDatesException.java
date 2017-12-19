package portal.errors;

public class InvalidTicketsDatesException  extends Exception {
    public InvalidTicketsDatesException(String message) {
        super(message);
    }
    
    public InvalidTicketsDatesException() {
        super("End date can not be before start date for adding tickets, dates must be in future time!");
    }
}

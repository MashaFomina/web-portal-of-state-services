package portal.errors;

public class InvalidAppointmentDateException extends Exception {
    public InvalidAppointmentDateException(String message) {
        super(message);
    }
    
    public InvalidAppointmentDateException() {
        super("Appointment date can not be before current date!");
    }
}

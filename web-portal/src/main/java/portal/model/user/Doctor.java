package portal.model.user;

import java.util.Calendar;
import java.util.Date;

import portal.errors.InvalidTicketsDatesException;
import portal.errors.NoRightsException;
import javax.persistence.*;

import portal.model.entities.Feedback;
import portal.model.entities.Ticket;
import portal.model.institutions.MedicalInstitution;

@Entity
@Table(name = "doctors")
public class Doctor extends User implements InstitutionRepresentativeInterface {
    private String position;
    private String summary;
    @ManyToOne
    @JoinColumn(name = "institution_id")
    private MedicalInstitution institution;
    private boolean approved;

    public Doctor() {}

    public Doctor(String username, String password, String fullName, String email, MedicalInstitution institution, String position, String summary, boolean approved) {
        super(username, password, fullName, email);
        this.position = position;
        this.summary = summary;
        this.institution = institution;
        this.approved = approved;
    }

    public Doctor(Doctor user) {
        super(user);
        this.position = user.position;
        this.summary = user.summary;
        this.institution =  user.institution;
        this.approved = user.approved;
    }

    public String getPosition() { return position; }

    public void setPosition(String position) { this.position = position; }

    public String getSummary() { return summary; }

    public void setSummary(String summary) { this.summary = summary; }

    @Override
    public MedicalInstitution getInstitution() { return institution; }

    public void setInstitution(MedicalInstitution institution) { this.institution = institution; }

    @Override
    public boolean isApproved() { return approved; }

    public void setApproved(boolean approved) { this.approved = approved; }

    public void addTickets(Date start, Date end, int intervalMinutes) throws InvalidTicketsDatesException, NoRightsException {
        Date currentDate = new Date();
        if (end.before(start) || start.before(currentDate)) {
            throw new InvalidTicketsDatesException();
        }
        if (!(start.getYear() == end.getYear() && start.getDay() == end.getDay()) || !(7 < start.getHours() && start.getHours() < end.getHours() && end.getHours() < 21)) {
            throw new InvalidTicketsDatesException("You must determine start and end in one day between 7:00 and 21:00!");
        }
        if (intervalMinutes < 0) {
            throw new InvalidTicketsDatesException("You must determine valid minute interval!");
        }

        Date ticketDate = start;
        Calendar cal = Calendar.getInstance();
        while (ticketDate.before(end)) {
            institution.addTicket(new Ticket(this, ticketDate, institution));
            cal.setTime(ticketDate);
            cal.add(Calendar.MINUTE, intervalMinutes); //minus number would decrement the minutes
            ticketDate = cal.getTime();
        }
    }

    public void addTicket(Date date) throws NoRightsException, InvalidTicketsDatesException {
        Date currentDate = new Date();
        if (date.before(currentDate)) {
            throw new InvalidTicketsDatesException("You must determine date for ticket in future!");
        }
        institution.addTicket(new Ticket(this, date, institution));
    }

    public void deleteTicket(Ticket ticket) throws NoRightsException {
        if (!ticket.getDoctor().equals(this)) {
            throw new NoRightsException("You have no rights to delete tickets of other doctor!");
        }
        ticket.getUser().addNotification("Sorry, but your ticket to " + ticket.getDoctor().getFullName() + " in " + institution.getTitle() + " on " + ticket.getDate() + " was canceled!");
        Citizen user = ticket.getUser();
        if (user != null) {
            user.removeTicket(ticket);
        }
        institution.removeTicket(ticket);
    }

    public void confirmVisit(Ticket ticket, String summary) throws NoRightsException {
        if (!ticket.getDoctor().equals(this)) {
            throw new NoRightsException("You have no rights to confirm visits of other doctors!");
        }
        ticket.getUser().addNotification("Now your can leave feedback about your visit to " + institution.getTitle());
        ticket.setVisited(true, summary);
    }

    @Override
    public void approve(User user) throws NoRightsException {
        if (user.isAdministrator()) {
            throw new NoRightsException("Just administrator can approve representative!");
        }
        approved = true;
    }

    @Override
    public boolean addFeedback(String text) throws NoRightsException {
        Date date = new Date();
        Feedback feedback = new Feedback(date, this, institution, text);
        return institution.saveFeedback(feedback);
    }

    @Override
    public boolean addFeedbackTo(String text, User userTo) throws NoRightsException {
        Date date = new Date();
        Feedback feedback = new Feedback(date, this, institution, text, userTo);
        return institution.saveFeedback(feedback);
    }
}
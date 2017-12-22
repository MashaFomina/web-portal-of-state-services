package portal.model.user;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.annotations.DynamicUpdate;
import portal.errors.NoRightsException;
import portal.errors.InvalidTicketsDatesException;
import portal.model.institutions.MedicalInstitution;
import portal.model.entities.Ticket;
import org.apache.commons.lang.time.DateUtils;
import portal.model.entities.Feedback;
import portal.errors.AlreadyExistsException;

import javax.persistence.*;

//@Entity(name = "MedicalRepresentative")
@Table(name = "representatives")
//@SecondaryTable(name = "representatives")
//@SecondaryTable(name = "InstitutionRepresentative")
@DiscriminatorValue(value = "0")
@DynamicUpdate
public class MedicalRepresentative extends InstitutionRepresentative {
    /*@ManyToOne
    @JoinColumn(name = "institution_id")*/
    @OneToOne(mappedBy="institution", orphanRemoval=true, cascade = { CascadeType.ALL,CascadeType.PERSIST,CascadeType.MERGE })
    private MedicalInstitution institution;

    public MedicalRepresentative(String username, String password, String fullName, String email, MedicalInstitution institution, boolean approved) {
        super(username, password, fullName, email);
        this.institution = institution;
        this.approved = approved;
    }

    public MedicalRepresentative(MedicalRepresentative user) {
        super(user);
        this.institution = user.institution;
        this.approved = user.approved;
    }

    public void addDoctor(Doctor doctor) throws NoRightsException {
        if (!doctor.getInstitution().equals(institution)) {
            throw new NoRightsException("You have no rights to add doctor!");
        }
        institution.addDoctor(doctor);
    }

    public Set<Citizen> removeDoctor(Doctor doctor) throws NoRightsException {
        if (!doctor.getInstitution().equals(institution)) {
            throw new NoRightsException("You have no rights to remove this doctor!");
        }
        return institution.removeDoctor(doctor);
    }

    public void addTickets(Doctor doctor, Date start, Date end, int intervalMinutes) throws InvalidTicketsDatesException, NoRightsException {
        if (!doctor.getInstitution().equals(institution)) {
            throw new NoRightsException("You have no rights to add tickets for doctor from other institution!");
        }

        Date currentDate = new Date();
        if (end.before(start) || start.before(currentDate)) {
            throw new InvalidTicketsDatesException();
        }
        if (!(start.getYear() == end.getYear() && start.getDay() == end.getDay()) || !(7 < start.getHours() && (start.getHours() < end.getHours() || (start.getHours() == end.getHours() && start.getMinutes() < end.getMinutes())) && end.getHours() < 21)) {
            throw new InvalidTicketsDatesException("You must determine start and end in one day between 7:00 and 21:00!");
        }
        if (intervalMinutes < 0) {
            throw new InvalidTicketsDatesException("You must determine valid minute interval!");
        }

        Date ticketDate = start;
        Calendar cal = Calendar.getInstance();
        while (ticketDate.before(end)) {
            institution.addTicket(new Ticket(doctor, ticketDate));
            cal.setTime(ticketDate);
            cal.add(Calendar.MINUTE, intervalMinutes); //minus number would decrement the minutes
            ticketDate = cal.getTime();
        }
    }

    public void addTicket(Doctor doctor, Date date) throws NoRightsException, InvalidTicketsDatesException {
        if (!doctor.getInstitution().equals(institution)) {
            throw new NoRightsException("You have no rights to add tickets for doctor from other institution!");
        }
        Date currentDate = new Date();
        if (date.before(currentDate)) {
            throw new InvalidTicketsDatesException("You must determine date for ticket in future!");
        }
        institution.addTicket(new Ticket(doctor, date));
    }

    public void deleteTicket(Ticket ticket) throws NoRightsException {
        if (!ticket.getDoctor().getInstitution().equals(institution)) {
            throw new NoRightsException("You have no rights to delete tickets of doctor from other institution!");
        }

        Citizen user = ticket.getUser();
        if (user != null) {
            if (ticket.canBeRefused()) {
                String notification = "Sorry, but your ticket to " + ticket.getDoctor().getFullName() + " in " + institution.getTitle() + " on " + ticket.getDate() + " was canceled!";
                user.addNotification(notification);
            }
            user.removeTicket(ticket);
        }
        institution.removeTicket(ticket);
    }

    public Set<Citizen> deleteTickets(Doctor doctor, Date date) throws NoRightsException {
        if (!doctor.getInstitution().equals(institution)) {
            throw new NoRightsException("You have no rights to delete tickets of doctor from other institution!");
        }
        List<Ticket> set = institution.getTickets();
        Set<Citizen> citizensOfRemovedTicketsToUpdate = new HashSet<>();
        Iterator<Ticket> i = set.iterator();
        Citizen user;
        Ticket t;
        while (i.hasNext()) {
            t = i.next(); // must be called before you can call i.remove()
            if (t.getDoctor().equals(doctor) && (date == null || DateUtils.isSameDay(date, t.getDate()))) {
                user = t.getUser();
                if (user != null) {
                    if (t.canBeRefused()) {
                        String notification = "Sorry, but your ticket to " + t.getDoctor().getFullName() + " in " + institution.getTitle() + " on " + t.getDate() + " was canceled!";
                        user.addNotification(notification);
                        citizensOfRemovedTicketsToUpdate.add(user);
                    }
                    user.removeTicket(t);
                }
                i.remove();
            }
        }
        return citizensOfRemovedTicketsToUpdate;
    }

    public void confirmVisit(Ticket ticket, String summary) throws NoRightsException {
        if (!ticket.getDoctor().getInstitution().equals(institution)) {
            throw new NoRightsException("You have no rights to confirm visits of other institution!");
        }
        if (ticket.getUser() != null && !ticket.isVisited()) {
            String notification = "Now your can leave feedback about your visit to " + institution.getTitle();
            ticket.getUser().addNotification(notification);
        }
        ticket.setVisited(true, summary);
    }

    @Override
    public MedicalInstitution getInstitution() {
        return institution;
    }
}
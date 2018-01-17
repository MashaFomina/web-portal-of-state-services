package portal.model.institutions;

import java.util.*;
import javax.persistence.*;

import org.apache.commons.lang.time.DateUtils;
import portal.errors.InvalidTicketsDatesException;
import portal.model.entities.Feedback;
import portal.model.entities.Ticket;
import portal.errors.NoRightsException;
import portal.model.user.Doctor;
import portal.model.user.Citizen;
import portal.model.user.User;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Entity(name = "MedicalInstitution")
@DiscriminatorValue(value = "0")
@NamedQueries({
        @NamedQuery(name = "MedicalInstitution.getCities", query = "select DISTINCT i.city from MedicalInstitution i"),
        @NamedQuery(name = "MedicalInstitution.getCityDistricts", query = "select DISTINCT i.district from MedicalInstitution i where i.city = ?1"),
        @NamedQuery(name = "MedicalInstitution.findAllByDistrict", query = "select i from MedicalInstitution i WHERE i.city = ?1 and i.district = ?2")
})
public class MedicalInstitution extends Institution {
    @OneToMany(mappedBy="institution", orphanRemoval=true, cascade = { CascadeType.ALL,CascadeType.PERSIST,CascadeType.MERGE })
    @OrderBy("id DESC")
    private List<Doctor> doctors = new ArrayList<>();
    @OneToMany(mappedBy="institution", orphanRemoval=true, cascade = { CascadeType.ALL,CascadeType.PERSIST,CascadeType.MERGE })
    @OrderBy("id DESC")
    private List<Ticket> tickets = new ArrayList<>();
    @Transient
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public MedicalInstitution() {}

    public MedicalInstitution(String title, String city, String district, String telephone, String fax, String address) {
        super(title, city, district, telephone, fax, address);
    }

    public MedicalInstitution(MedicalInstitution institution) {
        super(institution);
    }


    public List<Doctor> getDoctors() {
        return doctors;
    }

    public void setDoctors(List<Doctor> doctors) {
        this.doctors = doctors;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    @Transient
    public boolean isEdu() { return false; }

    public Set<Citizen> removeDoctor(Doctor doctor) throws NoRightsException {
        if (!doctor.getInstitution().equals(this)) {
            throw new NoRightsException("You have no rights to remove this doctor!");
        }
        Set<Citizen> citizensOfRemovedTicketsToUpdate = new HashSet<>();
        // Remove tickets of doctor
        Iterator<Ticket> i = tickets.iterator();
        Citizen user;
        while (i.hasNext()) {
            Ticket t = i.next(); // must be called before you can call i.remove()
            if (t.getDoctor().equals(doctor)) {
                user = t.getUser();
                if (user != null) {
                    if (t.canBeRefused()) {
                        String notification = "Sorry, but your ticket to " + t.getDoctor().getFullName() + " in " + getTitle() + " on " + dateFormat.format(t.getDate()) + " was canceled!";
                        user.addNotification(notification);
                        citizensOfRemovedTicketsToUpdate.add(user);
                    }
                    t.getUser().removeTicket(t);
                }
                i.remove();
            }
        }
        // Remove doctor
        if (doctors.contains(doctor)) {
            doctors.remove(doctor);
        }
        return citizensOfRemovedTicketsToUpdate;
    }


    public void addTicket(Ticket ticket) throws NoRightsException {
        if (ticket.getInstitution() != null && !ticket.getInstitution().equals(this)) {
            throw new NoRightsException("You have no ability to add ticket of other institution to this institution!");
        }
        if (!tickets.contains(ticket)) {
            tickets.add(ticket);
        }
    }

    public void removeTicket(Ticket ticket) {
        if (tickets.contains(ticket)) {
            tickets.remove(ticket);
        }
    }

    @Transient
    public List<Ticket> getTickets(Doctor doctor) throws NoRightsException {
        if (!doctor.getInstitution().equals(this)) {
            throw new NoRightsException("You have no ability to get tickets for doctor from other institution!");
        }
        List<Ticket> doctorTickets = new ArrayList<>();
        Iterator<Ticket> i = tickets.iterator();
        Date currentDate = new Date();
        while (i.hasNext()) {
            Ticket t = i.next(); // must be called before you can call i.remove()
            if (t.getDoctor().equals(doctor)) { //&& t.getDate().after(currentDate)) {
                doctorTickets.add(t);
            }
        }
        return doctorTickets;
    }

    @Override
    public boolean saveFeedback(Feedback feedback) throws NoRightsException {
        if (!feedback.getInstitution().equals(this)) {
            throw new NoRightsException("You have no ability to add feedback of other institution to this institution!");
        }

        if (feedback.getUser() instanceof Citizen) {
            for (Ticket t : tickets) {
                if (t.getUser() != null && t.getUser().equals(feedback.getUser()) && t.isVisited()) {
                    feedbacks.add(feedback);
                    return true;
                }
            }
            //boolean result = StorageRepository.getInstance().canAddFeedbackToMedicalInstitution(feedback.getUser(), this);
            boolean result = true;
            if (!result) throw new NoRightsException("Citizen must visit medical institution for adding feedbacks!");
            else {
                feedbacks.add(feedback);
                return true;
            }
        }
        else if (feedback.getUser() instanceof User) {
            feedbacks.add(feedback);
            return true;
        }

        return false;
    }

    public void addDoctor(Doctor doctor) throws NoRightsException {
        if (!doctor.getInstitution().equals(this)) {
            throw new NoRightsException("You have no rights to add doctor!");
        }
        if (!doctor.getInstitution().equals(this)) {
            throw new NoRightsException("You have no ability to add doctor of other institution to this institution!");
        }
        if (!doctors.contains(doctor)) {
            doctors.add(doctor);
        }
    }

    public void addTickets(Doctor doctor, Date start, Date end, int intervalMinutes) throws InvalidTicketsDatesException, NoRightsException {
        if (!doctor.getInstitution().equals(this)) {
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
            this.addTicket(new Ticket(doctor, ticketDate, this));
            cal.setTime(ticketDate);
            cal.add(Calendar.MINUTE, intervalMinutes); //minus number would decrement the minutes
            ticketDate = cal.getTime();
        }
    }

    public void addTicket(Doctor doctor, Date date) throws NoRightsException, InvalidTicketsDatesException {
        if (!doctor.getInstitution().equals(this)) {
            throw new NoRightsException("You have no rights to add tickets for doctor from other institution!");
        }
        Date currentDate = new Date();
        if (date.before(currentDate)) {
            throw new InvalidTicketsDatesException("You must determine date for ticket in future!");
        }
        this.addTicket(new Ticket(doctor, date, this));
    }

    public void deleteTicket(Ticket ticket) throws NoRightsException {
        if (!ticket.getDoctor().getInstitution().equals(this)) {
            throw new NoRightsException("You have no rights to delete tickets of doctor from other institution!");
        }

        Citizen user = ticket.getUser();
        if (user != null) {
            if (ticket.canBeRefused()) {
                String notification = "Sorry, but your ticket to " + ticket.getDoctor().getFullName() + " in " + this.getTitle() + " on " + dateFormat.format(ticket.getDate()) + " was canceled!";
                user.addNotification(notification);
            }
            user.removeTicket(ticket);
        }
        this.removeTicket(ticket);
    }

    public Set<Citizen> deleteTickets(Doctor doctor, Date date) throws NoRightsException {
        if (!doctor.getInstitution().equals(this)) {
            throw new NoRightsException("You have no rights to delete tickets of doctor from other institution!");
        }
        List<Ticket> set = this.getTickets();
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
                        String notification = "Sorry, but your ticket to " + t.getDoctor().getFullName() + " in " + this.getTitle() + " on " + dateFormat.format(t.getDate()) + " was canceled!";
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
        if (!ticket.getDoctor().getInstitution().equals(this)) {
            throw new NoRightsException("You have no rights to confirm visits of other institution!");
        }
        if (ticket.getUser() != null && !ticket.isVisited()) {
            String notification = "Now your can leave feedback about your visit to " + this.getTitle();
            ticket.getUser().addNotification(notification);
        }
        ticket.setVisited(true, summary);
    }
}
package portal.model.institutions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import javax.persistence.*;

import portal.model.entities.Feedback;
import portal.model.entities.Ticket;
import portal.errors.NoRightsException;
import portal.model.user.Doctor;
import portal.model.user.Citizen;
import portal.model.user.User;

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

    public void addDoctor(Doctor doctor) throws NoRightsException {
        if (!doctor.getInstitution().equals(this)) {
            throw new NoRightsException("You have no ability to add doctor of other institution to this institution!");
        }
        if (!doctors.contains(doctor)) {
            doctors.add(doctor);
        }
    }

    public Set<Citizen> removeDoctor(Doctor doctor) throws NoRightsException {
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
                        String notification = "Sorry, but your ticket to " + t.getDoctor().getFullName() + " in " + getTitle() + " on " + t.getDate() + " was canceled!";
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
}
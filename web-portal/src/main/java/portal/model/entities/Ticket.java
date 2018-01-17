package portal.model.entities;

import portal.model.user.Citizen;
import portal.model.institutions.MedicalInstitution;
import portal.model.user.Doctor;

import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name = "tickets")
@NamedQueries({
        @NamedQuery(name = "Ticket.findAllForDoctor", query = "select t from Ticket t WHERE t.doctor.id = ?1 and t.date > ?2 order by id desc"),
        @NamedQuery(name = "Ticket.deleteTicketsByDoctor", query = "delete from Ticket t where t.doctor.id = ?1"),
        @NamedQuery(name = "Ticket.deleteTicketsByDoctorAndDate", query = "delete from Ticket t where t.doctor.id = ?1 and (t.date between ?2 and ?3)"),
        @NamedQuery(name = "Ticket.cancelTicketsForChild", query = "update Ticket t set t.child.id = null where t.child.id = ?1"),
})
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user")
    private Citizen user = null;
    @ManyToOne
    @JoinColumn(name = "child")
    private Child child = null;

    @ManyToOne
    @JoinColumn(name = "institution_id")
    private MedicalInstitution institution;
    @ManyToOne
    @JoinColumn(name = "doctor")
    private Doctor doctor;
    @Column(name = "ticket_date")
    private Date date;
    @Column(name = "visited")
    private boolean visited = false;
    private String summary = "";

    public Ticket() {}

    public Ticket(Doctor doctor, Date date, MedicalInstitution institution) {
        this.doctor = doctor;
        this.date = date;
        this.institution = institution;
    }

    public Ticket(Doctor doctor, Date date, boolean visited, Citizen user, String summary) {
        this.user = user;
        this.doctor = doctor;
        this.date = date;
        this.visited = visited;
        this.summary = summary;
    }

    public Ticket(Doctor doctor, Date date, boolean visited, Citizen user, Child child, String summary) {
        this.user = user;
        this.child = child;
        this.doctor = doctor;
        this.date = date;
        this.visited = visited;
        this.summary = summary;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Citizen getUser() {
        return user;
    }

    public void setUser(Citizen user) { this.user = user; }

    public Child getChild() {
        return child;
    }

    public void setChild(Child child) { this.child = child; }
    
    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
        this.institution = (MedicalInstitution) doctor.getInstitution();
    }
    
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) { this.date = date; }
    
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) { this.summary = summary; }

    public MedicalInstitution getInstitution() { return institution; }

    public void setInstitution(MedicalInstitution institution) { this.institution = institution; }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }
    
    public boolean canBeRefused() {
        Date currentDate = new Date();
        return date.after(currentDate);
    }

    public boolean canSetVisited() {
        return !canBeRefused() && !isVisited() && user != null;
    }

    @Transient
    public boolean isTicketForChild() {
        return (child != null);
    }

    // Returns false if ticket already busy
    public boolean acceptTicket(Citizen user) {
        if (this.user == null) {
            this.user = user;
        }
        else {
            return false;
        }
        return true;
    }

    // Returns false if ticket already busy, accept ticket for child
    public boolean acceptTicket(Citizen user, Child child) {
        if (this.user == null) {
            this.user = user;
            this.child = child;
        }
        else {
            return false;
        }
        return true;
    }

    public void refuseTicket() {
        this.user = null;
        this.child = null;
    }

    public void setVisited(boolean visited, String summary) {
        this.visited = visited;
        this.summary = summary;
    }

    @Override
    public boolean equals(Object obj) {
        if ( (obj == null) || (obj.getClass() != this.getClass()) ) return false;
        Ticket other = (Ticket)obj;
        return (id == other.getId() && 
                (child != null ? child.equals(other.getChild()) : other.getChild() == null) && 
                doctor.equals(other.getDoctor()) && 
                date.equals(other.getDate())
                );
    }
    
    @Override
    public int hashCode() {
        return Long.toString(id).hashCode();
    }
}

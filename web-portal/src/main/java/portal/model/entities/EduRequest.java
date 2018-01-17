package portal.model.entities;

import java.util.Date;

import javax.persistence.*;

import portal.model.user.Citizen;
import portal.model.institutions.EducationalInstitution;

@Entity
@Table(name = "edu_requests")
@NamedQuery(name = "EduRequest.deleteEduRequestsForChild", query = "delete from EduRequest e where e.child.id = ?1")
public class EduRequest {
    public enum Status {
        OPENED("OPENED"),
        ACCEPTED_BY_INSTITUTION("ACCEPTED_BY_INSTITUTION"),
        ACCEPTED_BY_PARENT("ACCEPTED_BY_PARENT"),
        REFUSED("REFUSED"),
        CHILD_IS_ENROLLED("CHILD_IS_ENROLLED");
        private String text;

        Status(String text) {
            this.text = text;
        }

        public String getText() {
            return this.text;
        }

        public String getBeautifulText() {
            String text = getText();
            if (text.equals("ACCEPTED_BY_INSTITUTION")) return "ACCEPTED BY INSTITUTION";
            if (text.equals("ACCEPTED_BY_PARENT")) return "ACCEPTED BY PARENT";
            if (text.equals("CHILD_IS_ENROLLED")) return "CHILD IS ENROLLED";
            return text;
        }

        public static Status fromString(String text) {
            for (Status b : Status.values()) {
                if (b.text.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Enumerated(EnumType.STRING)
    private Status status;
    @ManyToOne
    @JoinColumn(name = "child")
    private Child child;
    @ManyToOne
    @JoinColumn(name = "parent")
    private Citizen parent;
    @ManyToOne
    @JoinColumn(name = "institution_id")
    private EducationalInstitution institution;
    @Column(name = "creation_date")
    private Date creationDate;
    private Date appointment;
    @Column(name = "class_number")
    private int classNumber;

    public EduRequest() {}

    public EduRequest(Status status, Child child, Citizen parent, EducationalInstitution institution, Date creationDate, Date appointment, int classNumber) {
        if (status == null) {
            this.status = Status.OPENED;
        }
        else {
            this.status = status;
        }
        this.child = child;
        this.parent = parent;
        this.institution = institution;
        this.creationDate = creationDate;
        this.appointment = appointment;
        this.classNumber = classNumber;
    }
        
    public EduRequest(EduRequest request) {
        this.status = request.status;
        this.child = request.child;
        this.parent = request.parent;
        this.institution = request.institution;
        this.creationDate = request.creationDate;
        this.appointment = request.appointment;
        this.classNumber = request.classNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Child getChild() {
        return child;
    }

    public void setChild(Child child) { this.child = child; }

    public Citizen getParent() {
        return parent;
    }

    public void setParent(Citizen parent) { this.parent = parent; }

    public EducationalInstitution getInstitution() {
        return institution;
    }

    public void setInstitution(EducationalInstitution institution) { this.institution = institution; }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) { this.creationDate = creationDate; }

    public Date getAppointment() {
        return appointment;
    }

    public void setAppointment(Date appointment) { this.appointment = appointment; }

    public int getClassNumber() {
        return classNumber;
    }

    public void setClassNumber(int classNumber) { this.classNumber = classNumber; }

    public void changeStatus(Status status) {
        setStatus(status);
    }

    public void makeAppointment(Date appointment) {
        setAppointment(appointment);
    }

    public boolean isPassedAppointment() {
        Date currentDate = new Date();
        return (appointment != null ? appointment.before(currentDate): false);
    }

    public boolean isAcceptedByInstitution() {
        return (status.equals(Status.ACCEPTED_BY_INSTITUTION));
    }
    
    public boolean isAcceptedByParent() {
        return (status.equals(Status.ACCEPTED_BY_PARENT));
    }
    
    public boolean isOpened() {
        return (status.equals(Status.OPENED));
    }
    
    public boolean isClosed() {
        return (status.equals(Status.CHILD_IS_ENROLLED) || status.equals(Status.REFUSED));
    }
        
    public boolean isChildEnrolled() {
        return (status.equals(Status.CHILD_IS_ENROLLED));
    }

    @Override
    public boolean equals(Object obj) {
        if ( (obj == null) || (obj.getClass() != this.getClass()) ) return false;
        EduRequest other = (EduRequest) obj;
        return (id == other.getId() && 
                (child != null ? child.equals(other.getChild()) : other.getChild() == null) && 
                (institution != null ? institution.equals(other.getInstitution()) : other.getInstitution() == null) && 
                (creationDate != null ? creationDate.equals(other.getCreationDate()) : other.getCreationDate() == null)
                );
    }
    
    @Override
    public int hashCode() {
        return Long.toString(id).hashCode();
    }
}

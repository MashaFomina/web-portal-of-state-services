package portal.model.entities;

import portal.model.institutions.EducationalInstitution;
import portal.model.entities.ids.SeatsId;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "educational_institutions_seats")
@NamedQuery(name = "Seats.cleanSeats", query = "delete from Seats s where s.id.institutionId = ?1")
public class Seats {
    @EmbeddedId
    private SeatsId id;
    @ManyToOne
    @JoinColumn(name = "institution_id", insertable = false, updatable = false)
    private EducationalInstitution institution;
    private Integer seats;
    @Column(name = "busy_seats")
    private Integer busySeats;

    public Seats() {}

    public Seats(Integer classNumber, Integer seats, Integer busySeats, EducationalInstitution institution) {
        id = new SeatsId(institution.getId(), classNumber);
        this.seats = seats;
        this.busySeats = busySeats;
        this.institution = institution;
    }

    public SeatsId getId() { return id; }

    public void setId(SeatsId id) { this.id = id; }

    @Transient
    public Long getInstitutionId() {
        return institution.getId();
    }

    public EducationalInstitution getInstitution() {
        return institution;
    }

    public void setInstitution(EducationalInstitution institution) {
        this.institution = institution;
    }

    @Transient
    public Integer getClassNumber() {
        return id.getClassNumber();
    }

    public void setClassNumber(Integer classNumber) {
        id.setClassNumber(classNumber);
    }

    public Integer getSeats() {
        return seats;
    }

    public void setSeats(Integer seats) {
        this.seats = seats;
    }

    public Integer getBusySeats() {
        return busySeats;
    }

    public void setBusySeats(Integer busySeats) {
        this.busySeats = busySeats;
    }

    @Override
    public boolean equals(Object obj) {
        if ( (obj == null) || (obj.getClass() != this.getClass()) ) return false;
        Seats other = (Seats) obj;
        return (this.getInstitutionId() == other.getInstitutionId() && this.getClassNumber() == other.getClassNumber());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

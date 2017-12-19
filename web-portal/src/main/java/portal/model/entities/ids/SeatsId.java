package portal.model.entities.ids;

import javax.persistence.*;
import java.io.Serializable;

@Embeddable
public class SeatsId implements Serializable {

    @Column(name = "institution_id")
    private Long institutionId;

    @Column(name = "class_number")
    private Integer classNumber;

    public SeatsId() {
    }

    public SeatsId(Long institutionId, int classNumber) {
        this.institutionId = institutionId;
        this.classNumber = classNumber;
    }

    public Integer getClassNumber() {
        return classNumber;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public void setClassNumber(Integer classNumber) {
        this.classNumber = classNumber;
    }

    public void setInstitutionId(Long institutionId) {
        this.institutionId = institutionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeatsId)) return false;
        SeatsId that = (SeatsId) o;
        return (institutionId.equals(that.getInstitutionId()) && classNumber == that.getClassNumber());
    }

    @Override
    public int hashCode() {
        return ("inst: " + Long.toString(institutionId) + ",cN:" + Integer.toString(classNumber)).hashCode();
    }
}
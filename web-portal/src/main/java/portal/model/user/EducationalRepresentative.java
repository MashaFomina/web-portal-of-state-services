package portal.model.user;

import org.hibernate.annotations.Immutable;
import portal.model.entities.EduRequest;
import portal.errors.NoRightsException;
import portal.errors.InvalidAppointmentDateException;
import portal.model.institutions.EducationalInstitution;
import portal.errors.NoFreeSeatsException;
import portal.errors.InvalidDataForSavingSeatsException;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import portal.model.entities.Feedback;

import javax.persistence.*;

//@Entity(name = "EducationalRepresentative")
@Table(name = "representatives")
//@SecondaryTable(name = "representatives")
//@SecondaryTable(name = "InstitutionRepresentative")
@DiscriminatorValue(value = "1")
@Immutable
public class EducationalRepresentative extends InstitutionRepresentative {
    /*@ManyToOne
    @JoinColumn(name = "institution_id")
    */
    @OneToOne(mappedBy="institution", orphanRemoval=true, cascade = { CascadeType.ALL,CascadeType.PERSIST,CascadeType.MERGE })
    private EducationalInstitution institution;

    public EducationalRepresentative(String username, String password, String fullName, String email, EducationalInstitution institution, boolean approved) {
        super(username, password, fullName, email);
        this.institution = institution;
        this.approved = approved;
    }

    public EducationalRepresentative(EducationalRepresentative user) {
        super(user);
        this.institution = user.institution;
        this.approved = user.approved;
    }

    @Override
    public EducationalInstitution getInstitution() {
        return institution;
    }

    @Override
    public boolean isApproved() {
        return approved;
    }
}
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


    @Override
    public MedicalInstitution getInstitution() {
        return institution;
    }
}
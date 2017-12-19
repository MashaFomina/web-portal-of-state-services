package portal.model.user;

import portal.errors.NoRightsException;
import portal.model.entities.Feedback;
import portal.model.institutions.Institution;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "representatives")
@DiscriminatorColumn(name = "institution.is_edu", discriminatorType=DiscriminatorType.INTEGER)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class InstitutionRepresentative extends User implements InstitutionRepresentativeInterface {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;
    @ManyToOne
    @JoinColumn(name = "institution_id")
    protected Institution institution;
    protected boolean approved;

    public InstitutionRepresentative(String username, String password, String fullName, String email) {
        super(username, password, fullName, email);
    }
    public InstitutionRepresentative(InstitutionRepresentative user) {
        super(user);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Institution getInstitution() { return institution; }

    public void setInstitution(Institution institution) { this.institution = institution; }

    @Override
    public boolean isApproved() { return approved; }

    public void setApproved(boolean approved) { this.approved = approved; }

    @Override
    public void approve(User user) throws NoRightsException {
        if (!user.isAdministrator()) {
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

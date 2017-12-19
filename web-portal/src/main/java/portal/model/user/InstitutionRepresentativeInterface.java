package portal.model.user;

import portal.errors.NoRightsException;
import portal.model.institutions.Institution;

public interface InstitutionRepresentativeInterface {
    public Institution getInstitution();
    public boolean isApproved(); // Check if representative is approved by administrator or by representative (while saving)
    public void approve(User user) throws NoRightsException; // Approve by administrator
    public boolean addFeedback(String text) throws NoRightsException;
    public boolean addFeedbackTo(String text, User userTo) throws NoRightsException;
}

package portal.model.user;

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

@Entity(name = "EducationalRepresentative")
@DiscriminatorValue(value = "1")
public class EducationalRepresentative extends InstitutionRepresentative {
    @ManyToOne
    @JoinColumn(name = "institution_id")
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

    public boolean acceptEduRequest(EduRequest request) throws NoRightsException {
        if (!request.getInstitution().equals(this.getInstitution())) {
            throw new NoRightsException("You have no rights to accept this educational request!");
        }
        if (request.isOpened()) {
            request.changeStatus(EduRequest.Status.ACCEPTED_BY_INSTITUTION);
            return true;
        }
        return false;
    }

    public void makeAppointment(EduRequest request, Date date) throws NoRightsException, InvalidAppointmentDateException {
        if (!request.getInstitution().equals(this.getInstitution())) {
            throw new NoRightsException("You have no rights to accept this educational request!");
        }
        Date currentDate = new Date();
        if (!date.after(currentDate)) {
            throw new InvalidAppointmentDateException();
        }
        request.makeAppointment(date);
    }

    public void refuseEduRequest(EduRequest request) throws NoRightsException {
        if (!request.getInstitution().equals(this.getInstitution())) {
            throw new NoRightsException("You have no rights to accept this educational request!");
        }
        request.changeStatus(EduRequest.Status.REFUSED);
    }

    public boolean makeChildEnrolled(EduRequest request) throws NoRightsException, NoFreeSeatsException {
        if (!request.getInstitution().equals(this.getInstitution())) {
            throw new NoRightsException("You have no rights to accept this educational request!");
        }

        int classNumber = request.getClassNumber();
        if (request.isAcceptedByParent()) {
            synchronized (this) {
                if (institution.getFreeSeats(request.getClassNumber()) < 1) {
                    throw new NoFreeSeatsException();
                }
                institution.setSeats(classNumber, institution.getSeats(classNumber), institution.getBusySeats(classNumber) + 1);
            }
            request.changeStatus(EduRequest.Status.CHILD_IS_ENROLLED);
            Citizen parent = request.getParent();
            List<EduRequest> set = parent.getEduRequests();
            Iterator<EduRequest> i = set.iterator();
            while (i.hasNext()) {
                EduRequest r = i.next(); // must be called before you can call i.remove()
                if (!r.equals(request) && request.getChild().equals(r.getChild())) {
                    r.getInstitution().removeEduRequest(r);
                    i.remove();
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public EducationalInstitution getInstitution() {
        return institution;
    }

    public EducationalInstitution editInstitution(String title, String city, String district, String telephone, String fax, String address, Map<Integer, Integer> seats, Map<Integer, Integer> busySeats) {
        institution.edit(title, city, district, telephone, fax, address, seats, busySeats);
        return institution;
    }

    public void setSeats(int classNumber, int seats, int busySeats) throws InvalidDataForSavingSeatsException {
        if ((classNumber > 11 || classNumber < 1) || seats < 1 || busySeats < 1 || busySeats > seats)
            throw new InvalidDataForSavingSeatsException();
        institution.setSeats(classNumber, seats, busySeats);
    }

    @Override
    public boolean isApproved() {
        return approved;
    }
}
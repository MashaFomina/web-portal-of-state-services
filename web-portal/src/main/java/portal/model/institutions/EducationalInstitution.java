package portal.model.institutions;

import java.util.*;
import javax.persistence.*;

import portal.errors.InvalidAppointmentDateException;
import portal.errors.NoFreeSeatsException;
import portal.model.entities.EduRequest;
import portal.model.entities.Feedback;
import portal.errors.NoRightsException;
import portal.model.entities.Seats;
import portal.model.user.Citizen;

@Entity (name = "EducationalInstitution")
@DiscriminatorValue(value = "1")
@NamedQueries({
        @NamedQuery(name = "EducationalInstitution.getCities", query = "select DISTINCT i.city from EducationalInstitution i"),
        @NamedQuery(name = "EducationalInstitution.getCityDistricts", query = "select DISTINCT i.district from EducationalInstitution i where i.city = ?1"),
        @NamedQuery(name = "EducationalInstitution.findAllByDistrict", query = "select i from EducationalInstitution i WHERE i.city = ?1 and i.district = ?2") // and i.is_edu = 1
})
public class EducationalInstitution extends Institution {
    @Transient
    private Map<Integer, Integer> seats = new HashMap<>(); // key - class number, value - seats
    @Transient
    private Map<Integer, Integer> busySeats = new HashMap<>(); // key - class number, value - busy seats
    @OneToMany(mappedBy="institution", orphanRemoval=true, cascade = { CascadeType.ALL,CascadeType.PERSIST,CascadeType.MERGE }) // name of property in Seats
    protected List<Seats> listSeats = new ArrayList<>();
    @OneToMany(mappedBy="institution", orphanRemoval=true, cascade = { CascadeType.ALL,CascadeType.PERSIST,CascadeType.MERGE })
    @OrderBy("id DESC")
    private List<EduRequest> requests = new ArrayList<>();

    public EducationalInstitution() {}

    public EducationalInstitution(String title, String city, String district, String telephone, String fax, String address, Map<Integer, Integer> seats, Map<Integer, Integer> busySeats) {
        super(title, city, district, telephone, fax, address);
        if (seats != null && busySeats != null) {
            setSeats(seats, busySeats);
        }
    }

    public EducationalInstitution(String title, String city, String district, String telephone, String fax, String address) {
        super(title, city, district, telephone, fax, address);
    }

    public EducationalInstitution(EducationalInstitution institution) {
        super(institution);
        setSeats(institution.seats, institution.busySeats);
    }

    public void edit(String title, String city, String district, String telephone, String fax, String address, Map<Integer, Integer> seats, Map<Integer, Integer> busySeats) {
        this.edit(title, city, district, telephone, fax, address);
        if (seats != null && busySeats != null) {
            setSeats(seats, busySeats);
        }
    }

    public List<EduRequest> getEduRequests() {
        return requests;
    }

    public void setEduRequests(ArrayList<EduRequest> requests) {
        this.requests = requests;
    }

    public List<Seats> getListSeats() { return listSeats; }

    public void setListSeats(List<Seats> listSeats) {
        this.listSeats = listSeats;
    }

    @Transient
    public boolean isEdu() { return true; }

    @Transient
    public void arrangeMaps() {
        seats.clear();
        busySeats.clear();
        for (Seats s: listSeats) {
            int classNumber = s.getClassNumber();
            seats.put(classNumber, s.getSeats());
            busySeats.put(classNumber, s.getBusySeats());
        }
    }

    @Transient
    public  Map<Integer, Integer> getSeats() {
        if (listSeats.size() != seats.size())
            arrangeMaps();
        return seats;
    }

    @Transient
    public  Map<Integer, Integer> getBusySeats() {
        if (listSeats.size() != busySeats.size())
            arrangeMaps();
        return busySeats;
    }

    @Transient
    public int getSeats(int classNumber) {
        if (listSeats.size() != seats.size())
            arrangeMaps();
        if (seats.containsKey(classNumber)) {
            return seats.get(classNumber);
        }
        return 0;
    }

    @Transient
    public int getBusySeats(int classNumber) {
        if (listSeats.size() != busySeats.size())
            arrangeMaps();
        if (busySeats.containsKey(classNumber)) {
            return busySeats.get(classNumber);
        }
        return 0;
    }

    @Transient
    public int getFreeSeats(int classNumber) {
        if (listSeats.size() != busySeats.size())
            arrangeMaps();
        if (busySeats.containsKey(classNumber) && seats.containsKey(classNumber)) {
            return (seats.get(classNumber) - busySeats.get(classNumber));
        }
        return 0;
    }

    public void setSeats(int classNumber, int seats, int busySeats) {
        this.seats.put(classNumber, seats);
        this.busySeats.put(classNumber, busySeats);
        Seats s = new Seats(classNumber, seats, busySeats, this);
        int i = listSeats.lastIndexOf(s);
        if (i > -1) {
            listSeats.remove(i);
        }
        listSeats.add(s);
    }

    public void setSeats(Map<Integer, Integer> seats, Map<Integer, Integer> busySeats) {
        this.seats = seats;
        this.busySeats = busySeats;
        listSeats.clear();
        for (Map.Entry<Integer, Integer> entry : seats.entrySet()) {
            Integer cN = entry.getKey();
            if (busySeats.containsKey(cN)) {
                listSeats.add(new Seats(cN, entry.getValue(), busySeats.get(cN), this));
            }
        }
    }

    public void addEduRequest(EduRequest request) throws NoRightsException {
        if (!request.getInstitution().equals(this)) {
            throw new NoRightsException("You have no ability to add educational request with other institution to this institution!");
        }
        requests.add(request);
    }

    public boolean createEduRequest(EduRequest request) throws NoRightsException {
        if (!request.getInstitution().equals(this)) {
            throw new NoRightsException("You have no ability to add educational request with other institution to this institution!");
        }
        boolean result = false;
        if (getFreeSeats(request.getClassNumber()) > 0) {
            requests.add(request);
            result = true;
        }
        return result;
    }

    public void removeEduRequest(EduRequest request) {
        if (requests.contains(request)) {
            requests.remove(request);
        }
    }

    @Override
    public boolean saveFeedback(Feedback feedback) throws NoRightsException {
        if (!feedback.getInstitution().equals(this)) {
            throw new NoRightsException("You have no ability to add feedback of other institution to this institution!");
        }
        feedbacks.add(feedback);
        return true;
    }

    public boolean acceptEduRequest(EduRequest request) throws NoRightsException {
        if (!request.getInstitution().equals(this)) {
            throw new NoRightsException("You have no rights to accept this educational request!");
        }
        if (request.isOpened()) {
            request.changeStatus(EduRequest.Status.ACCEPTED_BY_INSTITUTION);
            return true;
        }
        return false;
    }

    public void refuseEduRequest(EduRequest request) throws NoRightsException {
        if (!request.getInstitution().equals(this)) {
            throw new NoRightsException("You have no rights to accept this educational request!");
        }
        request.changeStatus(EduRequest.Status.REFUSED);
    }

    public boolean makeChildEnrolled(EduRequest request) throws NoRightsException, NoFreeSeatsException {
        if (!request.getInstitution().equals(this)) {
            throw new NoRightsException("You have no rights to accept this educational request!");
        }

        int classNumber = request.getClassNumber();
        if (request.isAcceptedByParent()) {
            synchronized (this) {
                if (getFreeSeats(request.getClassNumber()) < 1) {
                    throw new NoFreeSeatsException();
                }
                setSeats(classNumber, getSeats(classNumber), getBusySeats(classNumber) + 1);
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

    public void makeAppointment(EduRequest request, Date date) throws NoRightsException, InvalidAppointmentDateException {
        if (!request.getInstitution().equals(this)) {
            throw new NoRightsException("You have no rights to accept this educational request!");
        }
        Date currentDate = new Date();
        if (!date.after(currentDate)) {
            throw new InvalidAppointmentDateException();
        }
        request.setAppointment(date);
    }
}
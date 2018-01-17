package portal.model.user;


import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import org.springframework.format.annotation.DateTimeFormat;

import portal.model.entities.*;
import portal.errors.NoFreeSeatsException;
import portal.model.institutions.*;
import portal.errors.NoRightsException;

@Entity
@Table(name = "citizens")
public class Citizen extends User {
    private String policy;
    private String passport;
    @Column(name = "birth_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthDate;
    @OneToMany(mappedBy="parent", orphanRemoval=true, cascade = { CascadeType.ALL,CascadeType.PERSIST,CascadeType.MERGE })
    @OrderBy("id DESC")
    private List<EduRequest> requests = new ArrayList<>();
    @OneToMany(mappedBy="user", orphanRemoval=true, cascade = { CascadeType.ALL,CascadeType.PERSIST,CascadeType.MERGE })
    @OrderBy("id DESC")
    private List<Ticket> tickets = new ArrayList<>();
    @OneToMany(mappedBy="parent", orphanRemoval=true, cascade = { CascadeType.ALL,CascadeType.PERSIST,CascadeType.MERGE})
    @MapKey(name = "birthCertificate")
    @ElementCollection(targetClass = Child.class)
    @MapKeyClass(String.class)
    private Map<String, Child> childs = new HashMap<>();

    public Citizen() {}

    public Citizen(String username, String password, String fullName, String email, String policy, String passport, Date birthDate) {
        super(username, password, fullName, email);
        this.policy = policy;
        this.passport = passport;
        this.birthDate = birthDate;
    }

    public Citizen(Citizen user) {
        super(user);
        this.policy = user.policy;
        this.passport = user.passport;
        this.birthDate = user.birthDate;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String getPassport() {
        return passport;
    }

    public void setPassport(String passport) {
        this.passport = passport;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public List<EduRequest> getEduRequests() { return requests; }

    public void setEduRequests(List<EduRequest> requests) { this.requests = requests; }

    public List<Ticket> getTickets() { return tickets; }

    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }

    public Map<String, Child> getChilds() { return childs; }

    public void setChilds(Map<String, Child> childs) { this.childs = childs; }

    public void addChild(Child child) {
        if (child.getParent().equals(this)) {
            childs.put(child.getBirthCertificate(), child);
        }
    }

    public void addTicket(Ticket ticket) {
        if (ticket.getUser().equals(this)) {
            tickets.add(ticket);
        }
    }

    public void addEduRequest(EduRequest request) {
        if (request.getParent().equals(this)) {
            requests.add(request);
        }
    }

    public Child createChildInfo(String fullName, String birthCertificate, Date birthDate) {
        Child child = new Child(this, fullName, birthCertificate, birthDate);
        Date currentDate = new Date();
        if (currentDate.after(birthDate) && !childs.containsKey(child.getBirthCertificate())) {
            childs.put(child.getBirthCertificate(), child);
            return child;
        }
        return null;
    }

    @Transient
    public Child getChild(String birthCertificate) {
        if (childs.containsKey(birthCertificate)) {
            return childs.get(birthCertificate);
        }
        return null;
    }

    public boolean removeChildInfo(Child child) throws NoRightsException {
        String birthCertificate = child.getBirthCertificate();
        if (!child.getParent().equals(this)) {
            throw new NoRightsException("You have no rights to delete information about this child!");
        }
        if (childs.containsKey(birthCertificate)) {
            Iterator<EduRequest> i = requests.iterator();
            while (i.hasNext()) {
                EduRequest request = i.next(); // must be called before you can call i.remove()
                if (request.getChild().getBirthCertificate().equals(birthCertificate)) {
                    request.getInstitution().removeEduRequest(request);
                    i.remove();
                }
            }
            Iterator<Ticket> k = tickets.iterator();
            while (k.hasNext()) {
                Ticket ticket = k.next(); // must be called before you can call i.remove()
                if (ticket.getChild() != null && ticket.getChild().getBirthCertificate().equals(birthCertificate)) {
                    ticket.getInstitution().removeTicket(ticket);
                    k.remove();
                }
            }
            childs.remove(birthCertificate);
            return true;
        }
        return false;
    }

    public EduRequest createEduRequest(Child child, EducationalInstitution institution, int classNumber)  throws NoRightsException, NoFreeSeatsException {
        if (!child.getParent().equals(this)) {
            throw new NoRightsException("You have no rights to create educational request for child!");
        }

        if (institution.getFreeSeats(classNumber) < 1) {
            throw new NoFreeSeatsException();
        }

        if (childs.containsKey(child.getBirthCertificate())) {
            for (EduRequest request : requests) {
                // Child can be enrolled in just one institution
                if (request.getChild().equals(child) && request.isChildEnrolled()) {
                    return null;
                }
                // Request in institution already exists
                if (request.getChild().equals(child) && request.getInstitution().equals(institution)) {
                    return null;
                }
            }
            Date creationDate = new Date();
            EduRequest request = new EduRequest(null, child, this, institution, creationDate, null, classNumber);
            boolean result = institution.createEduRequest(request);
            if (result) {
                requests.add(request);
                return request;
            }
        }
        return null;
    }

    public void removeEduRequest(EduRequest request) throws NoRightsException {
        if (!request.getParent().equals(this)) {
            throw new NoRightsException("You have no rights to delete educational request!");
        }
        if (requests.contains(request)) {
            request.getInstitution().removeEduRequest(request);
            requests.remove(request);
        }
    }

    public boolean acceptEduRequest(EduRequest request) throws NoRightsException {
        if (!request.getParent().equals(this)) {
            throw new NoRightsException("You have no rights to accept educational request!");
        }
        if (requests.contains(request) && request.isAcceptedByInstitution()) {
            request.changeStatus(EduRequest.Status.ACCEPTED_BY_PARENT);
            return true;
        }
        return false;
    }

    public boolean acceptTicket(Ticket ticket) {
        boolean result = ticket.acceptTicket(this);
        if (result) {
            tickets.add(ticket);
        }
        return result;
    }

    public boolean acceptTicketForChild(Ticket ticket, Child child) throws NoRightsException {
        if (!child.getParent().equals(this)) {
            throw new NoRightsException("You have no rights to accept ticket for this child!");
        }
        boolean result = ticket.acceptTicket(this, child);
        if (result) {
            tickets.add(ticket);
        }
        return result;
    }

    public void cancelTicket(Ticket ticket) throws NoRightsException {
        if (!ticket.getUser().equals(this)) {
            throw new NoRightsException("You have no rights to refuse this ticket because your not owner!");
        }
        ticket.refuseTicket();
        /*if (tickets.contains(ticket)) {
            tickets.remove(ticket);
        }*/
    }

    public void removeTicket(Ticket ticket) throws NoRightsException {
        if (!ticket.getUser().equals(this)) {
            throw new NoRightsException("You have no rights to remove this ticket because your not owner!");
        }
        if (tickets.contains(ticket)) {
            tickets.remove(ticket);
        }
    }
}
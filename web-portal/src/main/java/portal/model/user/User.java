package portal.model.user;


import javax.persistence.*;
import java.util.Set;
import org.hibernate.validator.constraints.Email;
import java.util.ArrayList;

import portal.model.entities.Feedback;
import portal.model.entities.Notification;
import portal.model.institutions.Institution;

import java.util.List;
import java.util.Date;
import portal.errors.NoRightsException;
//import services.stateservices.service.NotificationEmaiService;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true)
    private String username;
    private String password;
    @Transient
    private String passwordConfirm;
    @Column(name = "full_name")
    private String fullName;
    @Email
    private String email;
    @ManyToMany
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;
    @OneToMany(mappedBy="owner", orphanRemoval=true, cascade = { CascadeType.ALL,CascadeType.PERSIST,CascadeType.MERGE })
    @OrderBy("id DESC")
    protected List<Notification> notifications;

    public User() {}

    public User(String username, String password, String fullName, String email) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.notifications = new ArrayList<Notification>();
    }

    public User(User user) {
        id = user.id;
        username = user.username;
        fullName = user.fullName;
        email = user.email;
        this.notifications = user.notifications;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public List<Notification> getNotifications() { return notifications; }

    public void setNotifications(List<Notification> notifications) { this.notifications = notifications; }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void addNotification(String notification) {
        /*NotificationEmaiService service = new NotificationEmaiService(email, notification);
        service.sendNotification();*/
        Date date = new Date();
        Notification n = new Notification(this, notification, date);
        notifications.add(n);
    }

    @Transient
    public boolean isDoctor() {
        boolean result = false;
        for (Role r: roles) {
            if (r.getName().equals("ROLE_DOCTOR")) {
                result = true;
            }
        }
        return result;
    }

    @Transient
    public boolean isAdministrator() {
        boolean result = false;
        for (Role r: roles) {
            if (r.getName().equals("ROLE_ADMIN")) {
                result = true;
            }
        }
        return result;
    }

    @Transient
    public boolean isCitizen() {
        boolean result = false;
        for (Role r: roles) {
            if (r.getName().equals("ROLE_CITIZEN")) {
                result = true;
            }
        }
        return result;
    }

    @Transient
    public boolean isEducationalRepresentative() {
        boolean result = false;
        for (Role r: roles) {
            if (r.getName().equals("ROLE_EDU_REPRESENTATIVE")) {
                result = true;
            }
        }
        return result;
    }

    @Transient
    public boolean isMedicalRepresentative() {
        boolean result = false;
        for (Role r: roles) {
            if (r.getName().equals("ROLE_MEDICAL_REPRESENTATIVE")) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return  username + ":" + fullName + "<" + email + ">";
    }

    public User getUser() {
        return this;
    }

    public boolean addFeedback(Institution institution, String text) throws NoRightsException {
        Date date = new Date();
        Feedback feedback = new Feedback(date, this, institution, text);
        return institution.saveFeedback(feedback);
    }

    public boolean addFeedbackTo(Institution institution, String text, User userTo) throws NoRightsException {
        Date date = new Date();
        Feedback feedback = new Feedback(date, this, institution, text, userTo);
        return institution.saveFeedback(feedback);
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || (obj.getClass() != this.getClass())) return false;
        User other = (User) obj;
        return username.equals(other.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
}
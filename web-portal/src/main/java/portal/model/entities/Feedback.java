package portal.model.entities;

import java.util.Date;

import javax.persistence.*;

import portal.model.user.User;
import portal.model.institutions.*;

@Entity
@Table(name = "feedbacks")
@NamedQuery(name = "Feedback.deleteFeedbacksForUser", query = "delete from Feedback f where f.user.id = ?1 or f.toUser.id = ?1")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user")
    private User user;
    @Column(name = "created")
    private Date date;
    @ManyToOne
    @JoinColumn(name = "institution_id")
    private Institution institution;
    @Column(name = "feedback_text")
    private String text;
    @ManyToOne
    @JoinColumn(name = "to_user")
    private User toUser = null;

    public Feedback() {}

    public Feedback(Date date, User user, Institution institution, String text) {
        this.date = date;
        this.user = user;
        this.institution = institution;
        this.text = text;
    }
        
    public Feedback(Date date, User user, Institution institution, String text, User toUser) {
        this.date = date;
        this.user = user;
        this.institution = institution;
        this.text = text;
        this.toUser = toUser;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) { this.date = date; }

    public Institution getInstitution() { return institution; }

    public void setInstitution(Institution institution) { this.institution = institution; }

    public User getUser() {
        return user;
    }

    public void setUser(User user) { this.user = user; }

    public User getToUser() {
        return toUser;
    }

    public void setToUser(User toUser) { this.toUser = toUser; }
    
    public String getText() {
        return text;
    }

    public void setText(String text) { this.text = text; }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(date);
        builder.append("] ");
        if (user != null) {
            builder.append(user.getFullName());
        }
        builder.append(": ");
        if (toUser != null) {
            builder.append(toUser).append(", ");
        }
        builder.append(text);
        return builder.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if ( (obj == null) || (obj.getClass() != this.getClass()) ) return false;
        Feedback other = (Feedback) obj;
        return (id == other.getId() && 
                text.equals(other.getText()) && 
                (user != null ? user.equals(other.getUser()) : other.getUser() == null) && 
                (institution != null ? institution.equals(other.getInstitution()) : other.getInstitution() == null) &&
                (toUser == null ? toUser.equals(other.getToUser()) : other.getToUser() == null) && 
                (date != null ? date.equals(other.getDate()) : other.getDate() == null)
                );
    }
    
    @Override
    public int hashCode() {
        return Long.toString(id).hashCode();
    }
}

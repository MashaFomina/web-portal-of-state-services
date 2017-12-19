package portal.model.entities;

import portal.model.user.User;

import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user")
    private User owner;

    private String notification;
    @Column(name = "created")
    private Date date;

    public Notification() {}

    public Notification(User owner, String notification, Date date) {
        this.owner = owner;
        this.notification = notification;
        this.date = date;
    }

    public Notification(Long id, String notification, Date date) {
        this.id = id;
        this.notification = notification;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() { return owner; }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getNotification() { return notification; }

    public void setNotification(String notification) { this.notification = notification; }

    public Date getDate() { return date; }

    public void setDate(Date date) { this.date = date; }

    @Override
    public String toString() {
        return date + ": " + notification;
    }
    
    @Override
    public boolean equals(Object obj) {
        if ( (obj == null) || (obj.getClass() != this.getClass()) ) return false;
        Notification other = (Notification)obj;
        return (id == other.getId() && 
                notification.equals(other.getNotification()) && 
                (date != null ? date.equals(other.getDate()): other.getDate() == null)
                );
    }
    
    @Override
    public int hashCode() {
        return Long.toString(id).hashCode();
    }
}

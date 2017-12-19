package portal.model.entities;

import portal.model.user.Citizen;

import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "childs")
public class Child {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "parent")
    private Citizen parent;
    @Column(name = "full_name")
    private String fullName;
    @Column(name = "birth_certificate")
    private String birthCertificate;
    @Column(name = "birth_date")
    private Date birthDate;

    public Child() {}

    public Child(Citizen parent, String fullName, String birthCertificate, Date birthDate) {
        this.parent = parent;
        this.fullName = fullName;
        this.birthCertificate = birthCertificate;
        this.birthDate = birthDate;
    }

    public Child(Long id, String fullName, String birthCertificate, Date birthDate) {
        this.id = id;
        this.fullName = fullName;
        this.birthCertificate = birthCertificate;
        this.birthDate = birthDate;
    }

    public Child(Child child) {
        this.id = child.id;
        this.parent = child.parent;
        this.fullName = child.fullName;
        this.birthCertificate = child.birthCertificate;
        this.birthDate = child.birthDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Citizen getParent() {
        return parent;
    }

    public void setParent(Citizen parent) {
        this.parent = parent;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBirthCertificate() { return birthCertificate;
    }

    public void setBirthCertificate(String birthCertificate) {
        this.birthCertificate = birthCertificate;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    @Override
    public boolean equals(Object obj) {
        if ( (obj == null) || (obj.getClass() != this.getClass()) ) return false;
        Child other = (Child) obj;
        return (id == other.getId() &&
                (parent != null ? parent.equals(other.getParent()) : other.getParent() == null) &&
                birthCertificate.equals(other.getBirthCertificate())
        );
    }

    @Override
    public int hashCode() {
        return Long.toString(id).hashCode();
    }
}
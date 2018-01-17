package portal.model.institutions;

import java.util.*;

import portal.errors.*;

import javax.persistence.*;

import portal.model.entities.Feedback;

@Entity
@Table(name = "institutions")
@DiscriminatorColumn(name = "is_edu", discriminatorType=DiscriminatorType.INTEGER)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NamedQueries({
        @NamedQuery(name = "Institution.getCities", query = "select DISTINCT i.city from Institution i"),
        @NamedQuery(name = "Institution.getCityDistricts", query = "select DISTINCT i.district from Institution i where i.city = ?1"),
        @NamedQuery(name = "Institution.findAllByDistrict", query = "select i from Institution i WHERE i.city = ?1 and i.district = ?2")
})
public abstract class Institution {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String title;
    private String city;
    private String district;
    private String telephone;
    private String fax;
    private String address;
    //@Column(name="is_edu")
    //private boolean isEdu;
    @OneToMany(mappedBy="institution", orphanRemoval=true, cascade = { CascadeType.ALL,CascadeType.PERSIST,CascadeType.MERGE })/*, cascade = {CascadeType.ALL}, targetEntity = Feedback.class)//Transaction.class)
    @Where(clause = "tableType='I'")*/
    @OrderBy("id DESC")
    protected List<Feedback> feedbacks = new ArrayList<>();

    public Institution() {}

    public Institution(String title, String city, String district, String telephone, String fax, String address) {
        this.title = title;
        this.city = city;
        this.district = district;
        this.telephone = telephone;
        this.fax = fax;
        this.address = address;
    }

    public Institution(Institution institution) {
        this.title = institution.title;
        this.city = institution.city;
        this.district = institution.district;
        this.telephone = institution.telephone;
        this.fax = institution.fax;
        this.address = institution.address;
    }

    public void edit(String title, String city, String district, String telephone, String fax, String address) {
        this.title = title;
        this.city = city;
        this.district = district;
        this.telephone = telephone;
        this.fax = fax;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getCity() { return city; }

    public void setCity(String city) { this.city = city; }

    public String getDistrict() { return district; }

    public void setDistrict(String district) { this.district = district; }

    public String getTelephone() { return telephone; }

    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getFax() { return fax; }

    public void setFax(String fax) { this.fax = fax; }

    public String getAddress() { return address; }

    public void setAddress(String address) { this.address = address; }

    @Transient
    public abstract boolean isEdu();

/*
    @Transient
    public boolean getIsEdu() { return isEdu; }

    public void setIsEdu(boolean isEdu) { this.isEdu = isEdu; }
*/
    public List<Feedback> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(List<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public abstract boolean saveFeedback(Feedback feedback) throws NoRightsException;

    public void addFeedback(Feedback feedback) { feedbacks.add(feedback); }

    @Override
    public boolean equals(Object obj) {
        if ( (obj == null) || (obj.getClass() != this.getClass()) ) return false;
        Institution other = (Institution) obj;
        return (id == other.getId() && title.equals(other.getTitle()) && telephone.equals(other.getTelephone()));
    }

    @Override
    public int hashCode() {
        return Long.toString(id).hashCode();
    }
}
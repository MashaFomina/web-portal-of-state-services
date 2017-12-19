package portal.services.projects;

public class Project {
    private long id;
    private String title;
    private String description;
    private String owner;
    private String repo;
    private String commit;
    private String branch;
    private String path;
    private long userid;

    public Project() {}

    public Project(String title, String description, String owner, String repo, String commit, String branch, String path, long userid){
        this.title = title;
        this.description = description;
        this.owner = owner;
        this.repo = repo;
        this.commit = commit;
        this.branch = branch;
        this.path = path;
        this.userid = userid;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String title) {
        this.owner = owner;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getPath() { return path; }

    public void setPath(String path) {
        this.path = path;
    }

    public long getUserid() {
        return userid;
    }

    public void setUserid(long userid) {
        this.userid = userid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Project project = (Project) o;

        if (id != project.id) return false;
        if (title != null ? !title.equals(project.title) : project.title != null) return false;
        if (description != null ? !description.equals(project.description) : project.description != null) return false;
        if (owner != null ? !owner.equals(project.owner) : project.owner != null) return false;
        if (repo != null ? !repo.equals(project.repo) : project.repo != null) return false;
        if (commit != null ? !commit.equals(project.commit) : project.commit != null) return false;
        if (branch != null ? !branch.equals(project.branch) : project.branch != null) return false;
        if (path != null ? !path.equals(project.path) : project.path != null) return false;
        if (userid != project.userid) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (repo != null ? repo.hashCode() : 0);
        result = 31 * result + (commit != null ? commit.hashCode() : 0);
        result = 31 * result + (branch != null ? branch.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (int) (userid ^ (userid >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", owner=" + owner + '\'' +
                ", repo=" + repo + '\'' +
                ", commit=" + commit + '\'' +
                ", branch=" + branch + '\'' +
                ", path=" + path + '\'' +
                ", userid=" + userid +
                '}';
    }
}
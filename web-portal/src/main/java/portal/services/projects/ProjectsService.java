package portal.services.projects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.dao.EmptyResultDataAccessException;

import java.sql.PreparedStatement;

@Component
public class ProjectsService {

    public static final String INSERT_PROJECT = "insert into projects (title, description, owner, repo, commit, branch, path, userid) values (?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String DELETE_PROJECT = "delete from projects where id = ?";
    public static final String UPDATE_PROJECT = "update projects set (title, description, owner, repo, commit, branch, path, userid) = (?, ?, ?, ?, ?, ?, ?, ?) where id = ?";
    public static final String SELECT_PROJECT = "select * from projects where id = ?";
    public static final String SELECT_USER_PROJECT = "select * from projects where userid = ?";
    public static final String SELECT_PROJECTS_COUNT = "select count(*) from projects where userid = ?";

    @Autowired
    JdbcTemplate jdbcTemplate;

    /*@Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }*/
    /*@Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }*/

    public int insertProject(Project project){
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rows = jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_PROJECT);
            preparedStatement.setString(1, project.getTitle());
            preparedStatement.setString(2, project.getDescription());
            preparedStatement.setString(3, project.getOwner());
            preparedStatement.setString(4, project.getRepo());
            preparedStatement.setString(5, project.getCommit());
            preparedStatement.setString(6, project.getBranch());
            preparedStatement.setString(7, project.getPath());
            preparedStatement.setLong(8, project.getUserid());
            return preparedStatement;
        }, keyHolder);
        project.setId(keyHolder.getKey().longValue());
        return rows;
    }

    public int deleteProject(long id){
        return jdbcTemplate.update(DELETE_PROJECT, preparedStatement -> preparedStatement.setLong(1, id));
    }

    public int updateProject(long id, Project project){
        return jdbcTemplate.update(UPDATE_PROJECT, project.getTitle(), project.getDescription(), project.getOwner(), project.getRepo(), project.getCommit(), project.getBranch(), project.getPath(), project.getUserid(), id);
    }

    public Project selectProject(long id){
        try {
            return jdbcTemplate.queryForObject(SELECT_PROJECT, new Object[]{id}, (resultSet, i) -> {
                Project project = new Project(
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        resultSet.getString("owner"),
                        resultSet.getString("repo"),
                        resultSet.getString("commit"),
                        resultSet.getString("branch"),
                        resultSet.getString("path"),
                        resultSet.getLong("userid")
                );
                System.out.println("description: " + resultSet.getString("description"));
                project.setId(resultSet.getLong("id"));
                return project;
            });
        }
        catch (EmptyResultDataAccessException e) {
            return new Project();
        }
    }

    public long selectProjectCount(long userid){
        long count = 0;
        count = jdbcTemplate.queryForObject(SELECT_PROJECTS_COUNT, new Object[] {userid}, Integer.class);
        return count;
    }

    /*public ArrayList<Project> selectUserProjects(long userid){
        ArrayList<Project> projects = new ArrayList<Project>();
        jdbcTemplate.queryForList(SELECT_USER_PROJECT, new Object[]{userid});
        , new Object[]{userid}, (resultSet, i) -> {
            Project project = new Project(
                    resultSet.getString("title"),
                    resultSet.getString("description"),
                    resultSet.getString("owner"),
                    resultSet.getString("repo"),
                    resultSet.getString("commit"),
                    resultSet.getString("branch"),
                    resultSet.getString("path"),
                    resultSet.getLong("userid")
            );
            project.setId(resultSet.getLong("id"));
            return project;
        });
    }*/
}
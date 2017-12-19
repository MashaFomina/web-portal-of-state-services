package portal.repositories.users;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.CrudRepository;
import portal.model.user.User;

@NoRepositoryBean
public interface UserBaseRepository<T extends User>  extends CrudRepository<T, Long> {

    public T findByEmail(String email);
    public T findByUsername(String username);
    public T findById(Long id);

}
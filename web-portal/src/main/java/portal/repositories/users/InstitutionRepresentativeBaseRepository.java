package portal.repositories.users;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import portal.model.user.InstitutionRepresentative;


@NoRepositoryBean
public interface InstitutionRepresentativeBaseRepository<T extends InstitutionRepresentative>  extends UserBaseRepository<T> { }
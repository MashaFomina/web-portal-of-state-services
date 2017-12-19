package portal.repositories.users;

import portal.model.user.EducationalRepresentative;
import javax.transaction.Transactional;

@Transactional
public interface EducationalRepresentativeRepository extends UserBaseRepository<EducationalRepresentative> {
}
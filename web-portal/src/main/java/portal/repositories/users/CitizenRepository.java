package portal.repositories.users;

import javax.transaction.Transactional;
import portal.model.user.Citizen;

@Transactional
public interface CitizenRepository extends UserBaseRepository<Citizen> { }
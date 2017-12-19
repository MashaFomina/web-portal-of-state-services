package portal.repositories.users;

import portal.model.user.MedicalRepresentative;
import javax.transaction.Transactional;

@Transactional
public interface MedicalRepresentativeRepository extends UserBaseRepository<MedicalRepresentative> { }
package portal.repositories.users;

import portal.model.user.Doctor;
import javax.transaction.Transactional;

@Transactional
public interface DoctorRepository extends UserBaseRepository<Doctor> { }
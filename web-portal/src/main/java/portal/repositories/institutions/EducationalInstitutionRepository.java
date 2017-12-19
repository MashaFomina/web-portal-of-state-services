package portal.repositories.institutions;

import portal.model.institutions.EducationalInstitution;

import javax.transaction.Transactional;
import java.util.ArrayList;

@Transactional
public interface EducationalInstitutionRepository extends InstitutionRepository<EducationalInstitution> {
}
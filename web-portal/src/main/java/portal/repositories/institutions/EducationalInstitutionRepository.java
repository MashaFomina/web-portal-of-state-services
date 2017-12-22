package portal.repositories.institutions;

import portal.model.institutions.EducationalInstitution;

import javax.transaction.Transactional;

@Transactional
public interface EducationalInstitutionRepository extends InstitutionBaseRepository<EducationalInstitution> {
}
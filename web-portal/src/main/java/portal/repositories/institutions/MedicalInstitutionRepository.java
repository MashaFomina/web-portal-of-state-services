package portal.repositories.institutions;

import portal.model.institutions.MedicalInstitution;

import javax.transaction.Transactional;

@Transactional
public interface MedicalInstitutionRepository extends InstitutionBaseRepository<MedicalInstitution> {
}
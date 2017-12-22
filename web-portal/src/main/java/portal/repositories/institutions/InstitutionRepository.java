package portal.repositories.institutions;

import portal.model.institutions.Institution;

import javax.transaction.Transactional;
import java.util.ArrayList;

//@Transactional
public interface InstitutionRepository extends InstitutionBaseRepository<Institution> {
    Institution findById(Long id);
    ArrayList<String> getCities();
    ArrayList<String> getCityDistricts(String city);
    ArrayList<Institution> findAllByDistrict(String city, String district);
}
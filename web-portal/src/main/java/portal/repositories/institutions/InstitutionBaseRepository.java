package portal.repositories.institutions;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import portal.model.institutions.Institution;

import java.util.ArrayList;

@NoRepositoryBean
public interface InstitutionBaseRepository<T extends Institution>  extends CrudRepository<T, Long> {
    T findById(Long id);
    ArrayList<String> getCities();
    ArrayList<String> getCityDistricts(String city);
    ArrayList<T> findAllByDistrict(String city, String district);
}

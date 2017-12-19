package portal.repositories.entities;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import portal.model.entities.EduRequest;

import javax.transaction.Transactional;

@Transactional
public interface EduRequestRepository extends CrudRepository<EduRequest, Long> {
    EduRequest findById(Long id);
    @Modifying
    void deleteEduRequestsForChild(Long childId);
}
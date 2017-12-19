package portal.repositories.entities;

import org.springframework.data.repository.CrudRepository;
import portal.model.entities.Child;

import javax.transaction.Transactional;

@Transactional
public interface ChildRepository extends CrudRepository<Child, Long> {
    Child findById(Long id);
}
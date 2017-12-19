package portal.repositories.entities;

import org.springframework.data.repository.CrudRepository;
import portal.model.entities.Notification;

import javax.transaction.Transactional;

@Transactional
public interface NotificationRepository extends CrudRepository<Notification, Long> {
    Notification findById(Long id);
}

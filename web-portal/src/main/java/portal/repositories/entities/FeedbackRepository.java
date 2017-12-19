package portal.repositories.entities;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import portal.model.entities.Feedback;

import javax.transaction.Transactional;

@Transactional
public interface FeedbackRepository extends CrudRepository<Feedback, Long> {
    Feedback findById(Long id);
    @Modifying
    void deleteFeedbacksForUser(Long userId);
}
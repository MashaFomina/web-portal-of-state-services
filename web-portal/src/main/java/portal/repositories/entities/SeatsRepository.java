package portal.repositories.entities;

import org.springframework.data.repository.CrudRepository;
import portal.model.entities.Seats;

import javax.transaction.Transactional;

@Transactional
public interface SeatsRepository  extends CrudRepository<Seats, Long> {
    void cleanSeats(Long institutionId);
}
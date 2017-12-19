package portal.repositories.entities;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import portal.model.entities.Ticket;

import java.util.ArrayList;
import java.util.Date;
import javax.transaction.Transactional;

@Transactional
public interface TicketRepository extends CrudRepository<Ticket, Long> {
    ArrayList<Ticket> findAllForDoctor(Long doctorId, Date startDateTime);
    Ticket findById(Long id);
    @Modifying
    void deleteTicketsByDoctor(Long id);
    @Modifying
    void deleteTicketsByDoctorAndDate(Long id, Date startDateTime, Date endDateTime);
    @Modifying
    void cancelTicketsForChild(Long id);
}
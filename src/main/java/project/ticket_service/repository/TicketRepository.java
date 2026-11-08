package project.ticket_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.ticket_service.model.Ticket;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByBookingUserId(Long userId);

    Optional<Ticket> findByTicketNumber(String ticketNumber);
}

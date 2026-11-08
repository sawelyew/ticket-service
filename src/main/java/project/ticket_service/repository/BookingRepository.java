package project.ticket_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ticket_service.dto.UserDtos.TicketReadDto;
import project.ticket_service.model.Booking;
import project.ticket_service.model.BookingStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);

    @Query("""
        SELECT new project.ticket_service.dto.UserDtos$TicketReadDto(
            t.id,
            t.ticketNumber,
            e.title,
            e.eventDate,
            e.venueName,
            s.rowNumber,
            s.seatNumber,
            s.price,
            t.isUsed
        )
        FROM Booking b
        JOIN b.user u
        JOIN b.seat s
        JOIN s.event e
        LEFT JOIN Ticket t ON t.booking.id = b.id
        WHERE u.email = :email
          AND b.status = project.ticket_service.model.BookingStatus.CONFIRMED
          AND e.eventDate > CURRENT_TIMESTAMP
        ORDER BY e.eventDate ASC
    """)
    List<TicketReadDto> findActiveTicketsByEmail(@Param("email") String email);

    @Query("""
        SELECT new project.ticket_service.dto.UserDtos$TicketReadDto(
            t.id,
            t.ticketNumber,
            e.title,
            e.eventDate,
            e.venueName,
            s.rowNumber,
            s.seatNumber,
            s.price,
            COALESCE(t.isUsed, false)
        )
        FROM Booking b
        JOIN b.user u
        JOIN b.seat s
        JOIN s.event e
        LEFT JOIN Ticket t ON t.booking.id = b.id
        WHERE u.email = :email
          AND (e.eventDate <= CURRENT_TIMESTAMP OR b.status = project.ticket_service.model.BookingStatus.CANCELLED)
        ORDER BY b.createdAt DESC
    """)
    Page<TicketReadDto> findHistoryTicketsByEmail(@Param("email") String email, Pageable pageable);

    boolean existsBySeatIdAndStatus(Long seatId, BookingStatus status);

    Optional<Booking> findBySeatIdAndStatus(Long seatId, BookingStatus status);
}
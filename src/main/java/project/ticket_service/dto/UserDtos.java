package project.ticket_service.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class UserDtos {

    public record UserProfileResponse(
            Long id,
            String email,
            boolean isVerified,
            OffsetDateTime createdAt
    ) {}

    public record TicketReadDto(
            Long ticketId,
            String ticketNumber,
            String eventTitle,
            OffsetDateTime eventDate,
            String venueName,
            int rowNumber,
            int seatNumber,
            BigDecimal price,
            boolean isUsed
    ) {}

    public record ActiveTicketsResponse(
            List<TicketReadDto> tickets
    ) {}

    public record PaginatedBookingsResponse(
            List<TicketReadDto> bookings,
            int page,
            int pageSize,
            long totalElements,
            int totalPages
    ) {}
}
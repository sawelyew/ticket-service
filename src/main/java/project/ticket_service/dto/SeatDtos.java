package project.ticket_service.dto;

import java.math.BigDecimal;
import java.util.List;

public class SeatDtos {

    public record SeatResponse(
            Long id,
            int rowNumber,
            int seatNumber,
            BigDecimal price
    ) {}

    public record SeatListResponse(
            List<SeatResponse> seats
    ) {}

    public record SeatCreateRequest(
            Integer rowNumber,
            Integer seatNumber,
            BigDecimal price
    ) {}
}
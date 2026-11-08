package project.ticket_service.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.List;

public class EventDtos {

    public record EventResponse(
            Long id,
            String title,
            String description,
            OffsetDateTime eventDate,
            String venueName,
            int totalSeats
    ){}

    public record PaginatedEventsResponse(
            List<EventResponse> events,
            int page,
            int pageSize,
            long totalElements,
            int totalPages
    ){}

    public record EventCreateUpdateDto(
            @NotBlank(message = "Название не может быть пустым")
            String title,

            String description,

            @NotNull(message = "Дата мероприятия обязательна")
            @Future(message = "Дата мероприятия должна быть в будущем")
            OffsetDateTime eventDate,

            @NotBlank(message = "Место не может быть пустым")
            String venueName,

            @Min(value = 1, message = "Количество мест должно быть больше или равно 1")
            int totalSeats
    ){}
}
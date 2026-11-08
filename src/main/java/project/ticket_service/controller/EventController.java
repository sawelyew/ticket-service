package project.ticket_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import project.ticket_service.dto.EventDtos.*;
import project.ticket_service.service.EventService;

import java.time.OffsetDateTime;

@Tag(name = "Events & Catalog", description = "Эндпоинты для просмотра афиши и мероприятий")
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @Operation(summary = "Получение списка мероприятий с фильтрацией и пагинацией")
    @GetMapping
    public ResponseEntity<PaginatedEventsResponse> getEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateTo,
            @RequestParam(required = false) String venueName,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ){
        PaginatedEventsResponse response = eventService.getEvents(dateFrom, dateTo, venueName, search, page, pageSize);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Детальная информация о конкретном мероприятии")
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id){
        EventResponse response = eventService.getEventById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Создание нового мероприятия (Только для ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventCreateUpdateDto dto){
        EventResponse response = eventService.createEvent(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Обновление мероприятия (Только для ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventCreateUpdateDto dto
    ){
        EventResponse response = eventService.updateEvent(id, dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Удаление мероприятия (Только для ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id){
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
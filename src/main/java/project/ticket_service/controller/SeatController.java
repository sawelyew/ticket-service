package project.ticket_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.ticket_service.dto.SeatDtos.*;
import project.ticket_service.service.SeatService;
import java.math.BigDecimal;

@Tag(name = "Seats Management", description = "Управление местами зала для мероприятий")
@RestController
@RequestMapping("/api/v1/events/{eventId}/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @Operation(summary = "Получение списка мест мероприятия")
    @GetMapping
    public ResponseEntity<SeatListResponse> getSeats(@PathVariable Long eventId) {
        SeatListResponse response = seatService.getSeatsByEvent(eventId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Добавление места к мероприятию (Только для ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<SeatResponse> createSeat(
            @PathVariable Long eventId,
            @RequestBody SeatCreateRequest request
    ) {
        SeatResponse response = seatService.createSeat(eventId, request.rowNumber(), request.seatNumber(), request.price());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Удаление конкретного места (Только для ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{seatId}")
    public ResponseEntity<Void> deleteSeat(@PathVariable Long seatId) {
        seatService.deleteSeat(seatId);
        return ResponseEntity.noContent().build();
    }
}
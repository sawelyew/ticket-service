package project.ticket_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import project.ticket_service.dto.BookingDtos.*;
import project.ticket_service.service.BookingService;

@Tag(name = "Booking Management", description = "Управление бронированием мест, инициализацией платежей и вебхуками банка")
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Operation(
            summary = "Резервирование места",
            description = "Занимает указанное место в Redis через распределенный лок (TTL 10 минут) при условии, что оно еще не выкуплено.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/reserve")
    public ResponseEntity<ReservationResponseDto> reserveSeat(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReservationRequest request
    ) {
        ReservationResponseDto response = bookingService.reserveSeat(userDetails.getUsername(), request.seatId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Инициация оплаты",
            description = "Создает бронь и платежную транзакцию в статусе PENDING, возвращая уникальный UUID платежа для эквайринга.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{seatId}/pay")
    public ResponseEntity<PaymentResponseDto> initiatePayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID места", required = true, example = "42")
            @PathVariable Long seatId
    ) {
        String paymentUuid = bookingService.initiatePayment(userDetails.getUsername(), seatId);
        PaymentResponseDto response = new PaymentResponseDto(paymentUuid, "PENDING");
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Вебхук от банка/эквайринга",
            description = "Обрабатывает callback с результатом платежа. При SUCCESS подтверждает бронь, создает билет и снимает лок. При FAILED переводит бронь в CANCELLED.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/webhook/bank")
    public ResponseEntity<Void> bankWebhook(@Valid @RequestBody BankWebhookPayload payload) {
        bookingService.handleBankWebhook(payload);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Отмена резерва",
            description = "Ручной сброс лока в Redis и перевод статуса брони в CANCELLED до момента оплаты.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/reserve/{seatId}")
    public ResponseEntity<Void> cancelReservation(
            @Parameter(description = "ID места", required = true, example = "42")
            @PathVariable Long seatId
    ) {
        bookingService.cancelReservation(seatId);
        return ResponseEntity.noContent().build();
    }
}
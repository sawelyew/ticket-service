package project.ticket_service.dto;

import jakarta.validation.constraints.NotNull;
import project.ticket_service.model.PaymentStatus;

import java.math.BigDecimal;

public class BookingDtos {

    public record ReservationRequest(
            @NotNull Long seatId
    ) {}

    public record ReservationResponseDto(
            Long seatId,
            Long expiresInSeconds
    ) {}

    public record PaymentInitiateRequest(
            @NotNull Long seatId,
            @NotNull BigDecimal amount
    ) {}

    public record BankWebhookPayload(
            @NotNull String paymentUuid,
            @NotNull Long seatId,
            @NotNull PaymentStatus status
    ) {}

    public record PaymentResponseDto(
            String paymentUuid,
            String status
    ) {}
}
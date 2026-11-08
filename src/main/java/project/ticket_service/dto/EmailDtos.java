package project.ticket_service.dto;

public class EmailDtos {

    public record EmailMessageDto(
            String email,
            String otpCode
    ) {}

    public record VerifyOtpRequest(
            String otpCode
    ) {}
}
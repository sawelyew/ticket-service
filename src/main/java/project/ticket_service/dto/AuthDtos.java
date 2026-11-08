package project.ticket_service.dto;

import java.time.OffsetDateTime;

public class AuthDtos {
    public record LoginRequest(
            String email,
            String password
    ){}

    public record RefreshTokenRequest(
            String refreshToken
    ){}

    public record RegisterRequest(
            String email,
            String password
    ){}

    public record TokenResponse (
            String accessToken,
            String refreshToken,
            String tokenType
    ){
        public TokenResponse(String accessToken, String refreshToken) {
            this(accessToken, refreshToken, "bearer");
        }
    }

    public record UserResponse(
            Long id,
            String email,
            OffsetDateTime createdAt
    ){}

}

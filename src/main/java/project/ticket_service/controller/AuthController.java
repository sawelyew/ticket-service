package project.ticket_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import project.ticket_service.dto.AuthDtos.*;
import project.ticket_service.dto.EmailDtos.*;
import project.ticket_service.service.AuthService;

@Tag(name = "Auth", description = "Модуль аутентификации: регистрация, вход и обновление сессий")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Регистрация нового пользователя", description = "Создает нового пользователя в системе со статусом не подтвержденного email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или пользователь с таким email уже существует")
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Вход в систему", description = "Аутентификация пользователя по email и паролю с выдачей пары JWT токенов (access и refresh).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный вход, возвращены токены"),
            @ApiResponse(responseCode = "401", description = "Неверный email или пароль")
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Обновление access токена", description = "Принимает валидный refresh токен, проверяет сессию в Redis (с защитой от Reuse Detection) и выдает новую пару токенов.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Токены успешно обновлены"),
            @ApiResponse(responseCode = "401", description = "Невалидный или просроченный refresh токен, либо попытка повторного использования (сессия сброшена)")
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
        TokenResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Запрос OTP-кода для подтверждения почты",
            description = "Генерирует 6-значный OTP-код, сохраняет в Redis и отправляет задачу в RabbitMQ для асинхронной рассылки письма. Требует Bearer токен.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Код успешно отправлен на почту"),
            @ApiResponse(responseCode = "400", description = "Почта уже подтверждена или пользователь не найден"),
            @ApiResponse(responseCode = "401", description = "Неавторизован (нет или неверный JWT-токен)")
    })
    @PostMapping("/send-verification")
    public ResponseEntity<String> sendVerificationCode(@AuthenticationPrincipal UserDetails userDetails) {
        authService.sendVerificationCode(userDetails.getUsername());
        return ResponseEntity.ok("Verification code sent to email successfully");
    }

    @Operation(
            summary = "Подтверждение почты по OTP-коду",
            description = "Проверяет введенный пользователем код из Redis. При успехе меняет статус is_verified на true.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Почта успешно подтверждена"),
            @ApiResponse(responseCode = "400", description = "Неверный или просроченный OTP-код"),
            @ApiResponse(responseCode = "401", description = "Неавторизован")
    })
    @PostMapping("/verify")
    public ResponseEntity<String> verifyEmail(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody VerifyOtpRequest request
    ) {
        authService.verifyEmail(userDetails.getUsername(), request.otpCode());
        return ResponseEntity.ok("Email successfully verified!");
    }
}
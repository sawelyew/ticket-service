package project.ticket_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import project.ticket_service.dto.UserDtos.*;
import project.ticket_service.service.UserService;


@Tag(name = "Users & Profile", description = "Управление профилем пользователя и историей билетов")
@RestController
@RequestMapping(name = "/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Получение профиля текущего пользователя", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails){
        UserProfileResponse profile = userService.getUserProfile(userDetails.getUsername());
        return ResponseEntity.ok(profile);
    }

    @Operation(summary = "Список активных (предстоящих) купленных билетов", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me/tickets/active")
    public ResponseEntity<ActiveTicketsResponse> getActiveTickets(@AuthenticationPrincipal UserDetails userDetails){
        ActiveTicketsResponse response = userService.getActiveTickets(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "История всех заказов (прошедшие события и отмененные брони)", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me/tickets/history")
    public ResponseEntity<PaginatedBookingsResponse> getTicketsHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ){
        PaginatedBookingsResponse response = userService.getTicketsHistory(userDetails.getUsername(), page, pageSize);
        return ResponseEntity.ok(response);
    }
}

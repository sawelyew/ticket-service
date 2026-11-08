package project.ticket_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ticket_service.dto.UserDtos.*;
import project.ticket_service.exception.ResourceNotFoundException;
import project.ticket_service.repository.BookingRepository;
import project.ticket_service.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String email){
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getIsVerified(),
                user.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public ActiveTicketsResponse getActiveTickets(String email){
        List<TicketReadDto> tickets = bookingRepository.findActiveTicketsByEmail(email);
        return new ActiveTicketsResponse(tickets);
    }

    @Transactional(readOnly = true)
    public PaginatedBookingsResponse getTicketsHistory(String email, int page, int pageSize){
        Page<TicketReadDto> ticketPage = bookingRepository.findHistoryTicketsByEmail(
                email,
                PageRequest.of(page, pageSize)
        );

        return new PaginatedBookingsResponse(
                ticketPage.getContent(),
                ticketPage.getNumber(),
                ticketPage.getSize(),
                ticketPage.getTotalElements(),
                ticketPage.getTotalPages()
        );
    }
}
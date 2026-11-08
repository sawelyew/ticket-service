package project.ticket_service.service;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ticket_service.dto.BookingDtos.*;
import project.ticket_service.exception.ResourceConflictException;
import project.ticket_service.exception.ResourceNotFoundException;
import project.ticket_service.model.*;
import project.ticket_service.repository.*;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final RedissonClient redissonClient;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final TicketRepository ticketRepository;

    private static final String LOCK_KEY_FORMAT = "lock:seat:%d";

    @Transactional
    public ReservationResponseDto reserveSeat(String email, Long seatId) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + seatId));

        boolean isAlreadySold = bookingRepository.existsBySeatIdAndStatus(seatId, BookingStatus.CONFIRMED);
        if (isAlreadySold) {
            throw new ResourceConflictException("This seat is already sold");
        }

        String lockKey = String.format(LOCK_KEY_FORMAT, seatId);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(0, 10, TimeUnit.MINUTES);
            if (!isLocked) {
                throw new ResourceConflictException("Seat is temporarily locked by another user");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread was interrupted during lock acquisition", e);
        }

        return new ReservationResponseDto(seatId, 600L);
    }

    @Transactional
    public String initiatePayment(String email, Long seatId) {
        String lockKey = String.format(LOCK_KEY_FORMAT, seatId);
        RLock lock = redissonClient.getLock(lockKey);

        if (!lock.isLocked()) {
            throw new ResourceConflictException("Reservation has expired or seat is not locked");
        }

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Booking booking = Booking.builder()
                .user(user)
                .seat(seat)
                .status(BookingStatus.PENDING)
                .build();
        bookingRepository.save(booking);

        String paymentUuid = UUID.randomUUID().toString();

        PaymentTransaction transaction = PaymentTransaction.builder()
                .booking(booking)
                .amount(seat.getPrice())
                .status(PaymentStatus.PENDING)
                .providerPaymentId(paymentUuid)
                .build();
        paymentTransactionRepository.save(transaction);

        return paymentUuid;
    }

    @Transactional
    public void handleBankWebhook(BankWebhookPayload payload) {
        PaymentTransaction transaction = paymentTransactionRepository.findByProviderPaymentId(payload.paymentUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Payment transaction not found for uuid: " + payload.paymentUuid()));

        if (transaction.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        Booking booking = transaction.getBooking();

        if (payload.status() == PaymentStatus.SUCCESS) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            transaction.setStatus(PaymentStatus.SUCCESS);
            paymentTransactionRepository.save(transaction);

            Ticket ticket = Ticket.builder()
                    .booking(booking)
                    .ticketNumber("TKT-" + UUID.randomUUID().toString().toUpperCase().substring(0, 8))
                    .isUsed(false)
                    .build();
            ticketRepository.save(ticket);

            String lockKey = String.format(LOCK_KEY_FORMAT, payload.seatId());
            RLock lock = redissonClient.getLock(lockKey);
            if (lock.isHeldByCurrentThread() || lock.isLocked()) {
                lock.forceUnlock();
            }

        } else {
            transaction.setStatus(PaymentStatus.FAILED);
            paymentTransactionRepository.save(transaction);

            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
        }
    }

    @Transactional
    public void cancelReservation(Long seatId) {
        String lockKey = String.format(LOCK_KEY_FORMAT, seatId);
        RLock lock = redissonClient.getLock(lockKey);

        if (lock.isLocked()) {
            lock.forceUnlock();
        }

        bookingRepository.findBySeatIdAndStatus(seatId, BookingStatus.PENDING)
                .ifPresent(booking -> {
                    booking.setStatus(BookingStatus.CANCELLED);
                    bookingRepository.save(booking);
                });
    }
}
package project.ticket_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ticket_service.dto.SeatDtos.*;
import project.ticket_service.exception.ResourceNotFoundException;
import project.ticket_service.model.Event;
import project.ticket_service.model.Seat;
import project.ticket_service.repository.EventRepository;
import project.ticket_service.repository.SeatRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public SeatListResponse getSeatsByEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event with id " + eventId + " not found");
        }

        List<SeatResponse> seatResponses = seatRepository.findByEventId(eventId).stream()
                .map(s -> new SeatResponse(s.getId(), s.getRowNumber(), s.getSeatNumber(), s.getPrice()))
                .toList();

        return new SeatListResponse(seatResponses);
    }

    @Transactional
    public SeatResponse createSeat(Long eventId, int rowNumber, int seatNumber, BigDecimal price) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event with id " + eventId + " not found"));

        Seat seat = Seat.builder()
                .event(event)
                .rowNumber(rowNumber)
                .seatNumber(seatNumber)
                .price(price)
                .build();

        Seat saved = seatRepository.save(seat);
        return new SeatResponse(saved.getId(), saved.getRowNumber(), saved.getSeatNumber(), saved.getPrice());
    }

    @Transactional
    public void deleteSeat(Long seatId) {
        if (!seatRepository.existsById(seatId)) {
            throw new ResourceNotFoundException("Seat with id " + seatId + " not found");
        }
        seatRepository.deleteById(seatId);
    }
}
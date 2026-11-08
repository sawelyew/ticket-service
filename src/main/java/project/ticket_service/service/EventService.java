package project.ticket_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ticket_service.dto.EventDtos.*;
import project.ticket_service.exception.ResourceNotFoundException;
import project.ticket_service.model.Event;
import project.ticket_service.repository.EventRepository;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public PaginatedEventsResponse getEvents(
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            String venueName,
            String search,
            int page,
            int pageSize
    ){
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("eventDate").ascending());
        Page<Event> eventPage = eventRepository.findEventsWithFilters(dateFrom, dateTo, venueName, search, pageRequest);

        var eventResponses = eventPage.getContent().stream()
                .map(e -> new EventResponse(
                                e.getId(),
                                e.getTitle(),
                                e.getDescription(),
                                e.getEventDate(),
                                e.getVenueName(),
                                e.getTotalSeats()
                        ))
                .toList();

        return new PaginatedEventsResponse(
                eventResponses,
                eventPage.getNumber(),
                eventPage.getSize(),
                eventPage.getTotalElements(),
                eventPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id){
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event with id " + id + " not found"));

        return mapToResponse(event);
    }

    @Transactional
    public EventResponse createEvent(EventCreateUpdateDto dto){
        Event event = Event.builder()
                .title(dto.title())
                .description(dto.description())
                .eventDate(dto.eventDate())
                .venueName(dto.venueName())
                .totalSeats(dto.totalSeats())
                .build();

        Event saved = eventRepository.save(event);
        return mapToResponse(saved);
    }

    @Transactional
    public EventResponse updateEvent(Long id, EventCreateUpdateDto dto) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event with id " + id + " not found"));

        event.setTitle(dto.title());
        event.setDescription(dto.description());
        event.setEventDate(dto.eventDate());
        event.setVenueName(dto.venueName());
        event.setTotalSeats(dto.totalSeats());

        Event updated = eventRepository.save(event);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event with id " + id + " not found");
        }
        eventRepository.deleteById(id);
    }

    private EventResponse mapToResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getEventDate(),
                event.getVenueName(),
                event.getTotalSeats()
        );
    }
}

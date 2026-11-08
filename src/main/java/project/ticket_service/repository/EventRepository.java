package project.ticket_service.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ticket_service.model.Event;

import java.time.OffsetDateTime;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("""
        SELECT e FROM Event e
        WHERE (CAST(:dateFrom AS timestamp) IS NULL OR e.eventDate >= :dateFrom)
          AND (CAST(:dateTo AS timestamp) IS NULL OR e.eventDate <= :dateTo)
          AND (:venueName IS NULL OR e.venueName = :venueName)
          AND (:search IS NULL OR e.title LIKE %:search%)
        """)
    Page<Event> findEventsWithFilters(
            @Param("dateFrom") OffsetDateTime dateFrom,
            @Param("dateTo") OffsetDateTime dateTo,
            @Param("venueName") String venueName,
            @Param("search") String search,
            Pageable pageable
    );
}
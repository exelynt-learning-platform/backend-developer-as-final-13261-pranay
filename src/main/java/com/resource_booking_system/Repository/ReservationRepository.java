package com.resource_booking_system.Repository;

import com.resource_booking_system.Entity.Reservation;
import com.resource_booking_system.Entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {


    @Query("""
            SELECT COUNT(r) > 0 FROM Reservation r
            WHERE r.resource.id = :resourceId
              AND r.status <> :excludedStatus
              AND r.startTime < :endTime
              AND r.endTime > :startTime
            """)
    boolean existsOverlappingReservation(
            @Param("resourceId") Long resourceId,
            @Param("excludedStatus") ReservationStatus excludedStatus,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
            );


    @Query("""
            SELECT COUNT(r) > 0 FROM Reservation r
            WHERE r.resource.id = :resourceId
              AND r.id <> :excludedId
              AND r.status <> :excludedStatus
              AND r.startTime < :endTime
              AND r.endTime > :startTime
            """)
    boolean existsOverlappingReservationExcludingId(
            @Param("resourceId") Long resourceId,
            @Param("excludedId") Long excludedId,
            @Param("excludedStatus") ReservationStatus excludedStatus,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
                   );

    @Override
    @EntityGraph(attributePaths = {"user", "resource"})
    Page<Reservation> findAll(Specification<Reservation> specification, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"user", "resource"})
    Optional<Reservation> findById(Long id);
}
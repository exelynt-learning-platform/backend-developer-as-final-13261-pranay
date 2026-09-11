package com.Resource_Booking_System.Repository;

import com.Resource_Booking_System.Entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation ,Long >, JpaSpecificationExecutor<Reservation>
{
    boolean existsByResourceIdAndStartTimeLessThanAndEndTimeGreaterThan (
            Long resourceId,
            LocalDateTime endTime,
            LocalDateTime startTime
    );

    boolean existsByResourceIdAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
            Long resourceId,
            Long id,
            LocalDateTime endTime,
            LocalDateTime startTime
    );

}

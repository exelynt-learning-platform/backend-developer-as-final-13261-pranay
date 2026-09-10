package com.Resource_Booking_System.Repository;

import com.Resource_Booking_System.Entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation ,Long >, JpaSpecificationExecutor<Reservation>
{

}

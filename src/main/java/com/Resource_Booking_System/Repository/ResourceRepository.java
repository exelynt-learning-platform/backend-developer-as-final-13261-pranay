package com.Resource_Booking_System.Repository;

import com.Resource_Booking_System.Entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends JpaRepository <Resource , Long> {

}

package com.resource_booking_system.Repository;

import com.resource_booking_system.Entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends JpaRepository <Resource , Long> {

}

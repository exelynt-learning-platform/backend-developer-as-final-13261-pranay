package com.Resource_Booking_System.Repository;

import com.Resource_Booking_System.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User , Long> {

    Optional <User> findByUsername (String username);

    boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
}

package com.resource_booking_system.Repository;

import com.resource_booking_system.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User , Long> {

    Optional <User> findByUsername (String username);

    boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
}

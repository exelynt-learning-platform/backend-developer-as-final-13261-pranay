package com.resource_booking_system.Configure;

import com.resource_booking_system.Entity.Role;
import com.resource_booking_system.Entity.User;
import com.resource_booking_system.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MyUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_whenUserExists_shouldReturnUserDetails() {

        User user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setEmail("john@x.com");
        user.setPassword("encoded");
        user.setRole(Role.USER);
        user.setEnabled(true);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("john");

        assertNotNull(result);
        assertEquals("john", result.getUsername());
        assertEquals("encoded", result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.getAuthorities().stream().anyMatch(a -> "USER".equals(a.getAuthority())));

        verify(userRepository).findByUsername("john");
    }

    @Test
    void loadUserByUsername_whenAdminUser_shouldReturnAdminAuthority() {

        User admin = new User();
        admin.setId(2L);
        admin.setUsername("admin");
        admin.setEmail("admin@x.com");
        admin.setPassword("encoded");
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        UserDetails result = userDetailsService.loadUserByUsername("admin");

        assertNotNull(result);
        assertTrue(result.getAuthorities().stream().anyMatch(a -> "ADMIN".equals(a.getAuthority())));
    }

    @Test
    void loadUserByUsername_whenUserDisabled_shouldReturnDisabledUserDetails() {

        User disabledUser = new User();
        disabledUser.setId(3L);
        disabledUser.setUsername("disabled");
        disabledUser.setEmail("disabled@x.com");
        disabledUser.setPassword("encoded");
        disabledUser.setRole(Role.USER);
        disabledUser.setEnabled(false);

        when(userRepository.findByUsername("disabled")).thenReturn(Optional.of(disabledUser));

        UserDetails result = userDetailsService.loadUserByUsername("disabled");

        assertNotNull(result);
        assertFalse(result.isEnabled(), "Disabled user should be marked as disabled");
    }

    @Test
    void loadUserByUsername_whenUserNotFound_shouldThrowException() {

        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("ghost"));

        verify(userRepository).findByUsername("ghost");
    }
}
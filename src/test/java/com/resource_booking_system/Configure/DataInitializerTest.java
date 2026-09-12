package com.resource_booking_system.Configure;

import com.resource_booking_system.Entity.Role;
import com.resource_booking_system.Entity.User;
import com.resource_booking_system.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {

        dataInitializer = new DataInitializer();

        ReflectionTestUtils.setField(dataInitializer, "adminUsername", "Pranay");
        ReflectionTestUtils.setField(dataInitializer, "adminEmail", "pranay@x.com");
        ReflectionTestUtils.setField(dataInitializer, "adminPassword", "Admin@123");
        ReflectionTestUtils.setField(dataInitializer, "userUsername", "Ram");
        ReflectionTestUtils.setField(dataInitializer, "userEmail", "ram@x.com");
        ReflectionTestUtils.setField(dataInitializer, "userPassword", "User@123");
        ReflectionTestUtils.setField(dataInitializer, "seedEnabled", true);
    }

    @Test
    void createUsers_whenNoUsersExist_shouldCreateBoth() throws Exception {

        when(userRepository.existsByUsername("Pranay")).thenReturn(false);
        when(userRepository.existsByUsername("Ram")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        CommandLineRunner runner = dataInitializer.createUsers(userRepository, passwordEncoder);
        runner.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(2)).save(captor.capture());

        User admin = captor.getAllValues().get(0);
        User user = captor.getAllValues().get(1);

        assertEquals("Pranay", admin.getUsername());
        assertEquals("pranay@x.com", admin.getEmail());
        assertEquals("encoded", admin.getPassword());
        assertEquals(Role.ADMIN, admin.getRole());
        assertTrue(admin.isEnabled());

        assertEquals("Ram", user.getUsername());
        assertEquals("ram@x.com", user.getEmail());
        assertEquals("encoded", user.getPassword());
        assertEquals(Role.USER, user.getRole());
        assertTrue(user.isEnabled());
    }

    @Test
    void createUsers_whenAdminExists_shouldCreateOnlyUser() throws Exception {

        when(userRepository.existsByUsername("Pranay")).thenReturn(true);
        when(userRepository.existsByUsername("Ram")).thenReturn(false);
        when(passwordEncoder.encode("User@123")).thenReturn("encoded");

        CommandLineRunner runner = dataInitializer.createUsers(userRepository, passwordEncoder);
        runner.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());

        assertEquals("Ram", captor.getValue().getUsername());
        assertEquals(Role.USER, captor.getValue().getRole());
    }

    @Test
    void createUsers_whenUserExists_shouldCreateOnlyAdmin() throws Exception {

        when(userRepository.existsByUsername("Pranay")).thenReturn(false);
        when(userRepository.existsByUsername("Ram")).thenReturn(true);
        when(passwordEncoder.encode("Admin@123")).thenReturn("encoded");

        CommandLineRunner runner = dataInitializer.createUsers(userRepository, passwordEncoder);
        runner.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());

        assertEquals("Pranay", captor.getValue().getUsername());
        assertEquals(Role.ADMIN, captor.getValue().getRole());
    }

    @Test
    void createUsers_whenBothExist_shouldCreateNone() throws Exception {

        when(userRepository.existsByUsername("Pranay")).thenReturn(true);
        when(userRepository.existsByUsername("Ram")).thenReturn(true);

        CommandLineRunner runner = dataInitializer.createUsers(userRepository, passwordEncoder);
        runner.run();

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void createUsers_shouldReturnNonNullRunner() {

        CommandLineRunner runner = dataInitializer.createUsers(userRepository, passwordEncoder);

        assertNotNull(runner);
    }

    @Test
    void createUsers_whenSeedDisabled_shouldNotCreateAnyUser() throws Exception {

        ReflectionTestUtils.setField(dataInitializer, "seedEnabled", false);

        CommandLineRunner runner = dataInitializer.createUsers(userRepository, passwordEncoder);
        runner.run();

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }
}
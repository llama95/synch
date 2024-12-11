package org.example.service;

import org.example.model.User;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUserUsernameExists() {
        // Set up a mocked user
        User user = new User();
        user.setUsername("existingUser");
        user.setPassword("password123");

        when(userRepository.findByUsername(user.getUsername())).thenReturn(new User());
        ResponseEntity response = userService.registerUser(user);

        // Assertions
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Username already exists.", response.getBody());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser() {
        // Set up a mocked use
        User user = new User();
        user.setUsername("newUser");
        user.setPassword("password123");

        when(userRepository.findByUsername(user.getUsername())).thenReturn(null);
        when(passwordEncoder.encode(user.getPassword())).thenReturn("password123");
        ResponseEntity response = userService.registerUser(user);

        // Assertions
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User registered successfully.", response.getBody());
        verify(passwordEncoder, times(1)).encode(user.getPassword());
        verify(userRepository, times(1)).save(user);
        assertEquals("password123", user.getPassword());
    }
}

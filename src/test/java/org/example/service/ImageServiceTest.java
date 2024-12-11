package org.example.service;

import org.example.model.Image;
import org.example.model.User;
import org.example.repository.ImageRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ImageServiceTest {

    @InjectMocks
    private ImageService imageService;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @Test
    void testViewImage() {
        // Set up a mocked image and user
        User user = new User();
        user.setUsername("john_doe");
        Image image = new Image();
        image.setId(1L);
        image.setLink("http://imgur.com/testimage");
        image.setUser(user);

        when(authentication.getName()).thenReturn("john_doe");
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
        ResponseEntity response = imageService.viewImageByDbId(1L);

        // Assertionss
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals("http://imgur.com/testimage", response.getHeaders().getLocation().toString());
    }

    @Test
    void testDeleteImage() {
        // Set up a mocked image and user
        User user = new User();
        user.setUsername("john_doe");
        Image image = new Image();
        image.setId(1L);
        image.setUser(user);
        when(authentication.getName()).thenReturn("john_doe");
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
        ResponseEntity response = imageService.deleteImageByDbId(1L);

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Image deleted successfully.", response.getBody());
        verify(imageRepository, times(1)).delete(image);
    }

    @Test
    void testGetUserImages() {
        // Set up a mocked image and user
        User user = new User();
        user.setId(1L);
        user.setUsername("john_doe");
        List<Image> images = List.of(new Image(), new Image());

        when(authentication.getName()).thenReturn("john_doe");
        when(userRepository.findByUsername("john_doe")).thenReturn(user);
        when(imageRepository.findByUserId(1L)).thenReturn(images);
        ResponseEntity response = imageService.getUserImages();

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(images, response.getBody());
    }
}

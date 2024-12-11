package org.example.service;

import org.example.model.Image;
import org.example.model.User;
import org.example.repository.ImageRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ImageService {

    @Value("${imgur.client-id}")
    private String imgurClientId;

    private final String IMGUR_API_URL = "https://api.imgur.com/3/image";

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KafkaProducer kafkaProducer;

    public ResponseEntity uploadImage(String filePath, String title, String description) {

        try {
            User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
            File file = new File("src/main/resources/static/uploads/" + filePath);

            if (!file.exists() && user != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File not found at: " + file.getAbsolutePath());
            }

            byte[] fileContent = Files.readAllBytes(file.toPath());
            String encodedFile = Base64.getEncoder().encodeToString(fileContent);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Client-ID " + imgurClientId);

            Map<String, String> body = Map.of("image", encodedFile, "title", title, "description", description);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map> response = restTemplate.exchange(IMGUR_API_URL, HttpMethod.POST, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                String imgurId = (String) data.get("id");
                String link = (String) data.get("link");

                Image image = new Image();
                image.setImgurId(imgurId);
                image.setLink(link);
                image.setTitle(title);
                image.setDescription(description);
                image.setUser(user);

                imageRepository.save(image);

                // Produce Kafka Event
                kafkaProducer.sendImageUploadEvent(user.getUsername(), title);

                return ResponseEntity.ok("Image uploaded successfully. Link: " + link);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading image: " + e.getMessage());
        }
        return null;

    }

    public ResponseEntity viewImageByDbId(Long id) {
        Optional<Image> image = imageRepository.findById(id);
        if (image.isPresent()) {
            // Make sure another user is not trying to view our images
            if (!image.get().getUser().getUsername().equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("This image does not belong to this user.");
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(image.get().getLink()));
            // Display the image via its url link
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found.");
        }
    }

    public ResponseEntity deleteImageByDbId(Long id) {
        Optional<Image> image = imageRepository.findById(id);
        if (image.isPresent()) {
            // Make sure another user is not trying to view our images
            if (!image.get().getUser().getUsername().equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("This image does not belong to this user.");
            }
            // Del the image
            imageRepository.delete(image.get());
            return ResponseEntity.ok("Image deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found.");
        }
    }

    public ResponseEntity getUserImages() {
        try {
            User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }
            // Get images assc to the logged in user
            List<Image> images = imageRepository.findByUserId(user.getId());
            if (images == null || images.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No images found for this user.");
            }
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred downloading images.");
        }
    }
}

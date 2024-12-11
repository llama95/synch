package org.example.controller;

import org.example.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

//    move the view img to service arch DONE
//    move the del img to service arch DONE
//    move the usr imgs to service arch DONE
//    move the user reg to service arch DONE
//    make sure logged in user can only see imgs he/she uploaded , not images assc to other users or be able to see other users images/delete other users images DONE
//    tests
//    postman collection
//    readME
//    view images assc to a user DONE

    @PostMapping("/upload")
    public ResponseEntity uploadImage(@RequestParam String filePath, @RequestParam String title, @RequestParam String description) {
        return imageService.uploadImage(filePath, title, description);
    }

    // View an image's link by database ID
    @GetMapping("/{id}")
    public ResponseEntity viewImageByDbId(@PathVariable Long id) {
        return imageService.viewImageByDbId(id);
    }

    // Delete an image by database ID
    @DeleteMapping("/{id}")
    public ResponseEntity deleteImageByDbId(@PathVariable Long id) {
        return imageService.deleteImageByDbId(id);
    }

    @GetMapping("/my-images")
    public ResponseEntity getUserImages() {
        return imageService.getUserImages();
    }
}

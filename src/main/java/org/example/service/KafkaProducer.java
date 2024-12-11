package org.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topic-name:user-events}")
    private String topicName;

    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendImageUploadEvent(String username, String imageName) {
        String message = String.format("{\"username\":\"%s\", \"imageName\":\"%s\"}", username, imageName);
        kafkaTemplate.send(topicName, message);
    }
}

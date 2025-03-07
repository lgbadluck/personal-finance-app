package com.softuni.personal_finance_app.service;

import com.softuni.personal_finance_app.web.dto.Notification;
import com.softuni.personal_finance_app.web.dto.NotificationRequest;
import com.softuni.personal_finance_app.web.dto.NotificationSettings;
import com.softuni.personal_finance_app.web.dto.UpsertNotificationSettings;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// url - основен endpoint
@FeignClient(name = "notification-svc", url = "http://localhost:8082/api/v1/notifications")
public interface NotificationClient {

    @GetMapping("/test")
    ResponseEntity<String> getHelloMessage(@RequestParam(name = "name") String name);

    @PostMapping("/settings")
    ResponseEntity<Void> upsertNotificationPreference(@RequestBody UpsertNotificationSettings notificationPreference);

    @GetMapping("/settings")
    ResponseEntity<NotificationSettings> getUserNotificationSettings(@RequestParam(name = "userId") UUID userId);

    @GetMapping
    ResponseEntity<List<Notification>> getNotificationHistory(@RequestParam(name = "userId")UUID userId);

    @PostMapping
    ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest notificationRequest);

    @PutMapping("/settings")
    ResponseEntity<Void> updateNotificationSettings(@RequestParam("userId") UUID userId, @RequestParam("enabled") boolean enabled);
}

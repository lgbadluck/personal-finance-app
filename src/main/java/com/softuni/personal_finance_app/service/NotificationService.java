package com.softuni.personal_finance_app.service;

import com.softuni.personal_finance_app.exception.FeignCallException;
import com.softuni.personal_finance_app.web.dto.Notification;
import com.softuni.personal_finance_app.web.dto.NotificationRequest;
import com.softuni.personal_finance_app.web.dto.NotificationSettings;
import com.softuni.personal_finance_app.web.dto.UpsertNotificationSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class NotificationService {

    private final NotificationClient notificationClient;

    @Value("${notification-svc.failure-message.clear-history}")
    private String clearHistoryFailedMessage;

    @Autowired
    public NotificationService(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    public void saveNotificationPreference(UUID userId, boolean isEmailEnabled, String email) {

        UpsertNotificationSettings notificationSettings = UpsertNotificationSettings.builder()
                .userId(userId)
                .contactInfo(email)
                .type("EMAIL")
                .notificationEnabled(isEmailEnabled)
                .build();

        // Invoke Feign client and execute HTTP Post Request.
        ResponseEntity<Void> httpResponse = notificationClient.upsertNotificationPreference(notificationSettings);
        if (!httpResponse.getStatusCode().is2xxSuccessful()) {
            log.error("[Feign call to notification-svc failed] Can't save user preference for user with id = [%s]".formatted(userId));
        }
    }

    public NotificationSettings getNotificationSettings(UUID userId) {

        ResponseEntity<NotificationSettings> httpResponse = notificationClient.getUserNotificationSettings(userId);

        if (!httpResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Notification preference for user id [%s] does not exist.".formatted(userId));
        }

        return httpResponse.getBody();
    }

    public List<Notification> getNotificationHistory(UUID userId) {

        ResponseEntity<List<Notification>> httpResponse = notificationClient.getNotificationHistory(userId);

        return httpResponse.getBody();
    }

    public void sendNotification(UUID userId, String emailSubject, String emailBody) {

        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(userId)
                .subject(emailSubject)
                .body(emailBody)
                .build();

        // Service to Service
        ResponseEntity<Void> httpResponse;
        try {
            httpResponse = notificationClient.sendNotification(notificationRequest);
            if (!httpResponse.getStatusCode().is2xxSuccessful()) {
                log.error("[Feign call to notification-svc failed] Can't send email to user with id = [%s]".formatted(userId));
            }
        } catch (Exception e) {
            log.warn("Can't send email to user with id = [%s] due to 500 Internal Server Error.".formatted(userId));
        }
    }

    public void updateNotificationSettings(UUID userId, boolean enabled) {

        try {
            notificationClient.updateNotificationSettings(userId, enabled);
        } catch (Exception e) {
            log.warn("Can't update notification preferences for user with id = [%s].".formatted(userId));
        }
    }

    public void clearHistory(UUID userId) {

        try {
            notificationClient.clearHistory(userId);
        } catch (Exception e) {
            log.error("Unable to call notification-svc for clear notification history.");
            throw new FeignCallException(clearHistoryFailedMessage);
        }
    }

    public void retryFailed(UUID userId) {

        try {
            notificationClient.retryFailedNotifications(userId);
        } catch (Exception e) {
            log.error("Unable to call notification-svc for re-send of failed notifications.");
            throw new FeignCallException(clearHistoryFailedMessage);
        }
    }
}

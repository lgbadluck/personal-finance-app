package com.softuni.personal_finance_app.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.softuni.personal_finance_app.exception.FeignCallException;
import com.softuni.personal_finance_app.web.dto.Notification;
import com.softuni.personal_finance_app.web.dto.NotificationRequest;
import com.softuni.personal_finance_app.web.dto.NotificationSettings;
import com.softuni.personal_finance_app.web.dto.UpsertNotificationSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class NotificationServiceUTest {

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void happyPath_whenSaveNotificationPreference() {

        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@email.com";
        UpsertNotificationSettings notificationSettings = UpsertNotificationSettings.builder()
                .userId(userId)
                .contactInfo(email)
                .type("EMAIL")
                .notificationEnabled(true)
                .build();
        ResponseEntity<Void> mockResponse;
        mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(notificationClient.upsertNotificationPreference(any(UpsertNotificationSettings.class))).thenReturn(mockResponse);

        // When
        notificationService.saveNotificationPreference(userId, true, email);

        // Then
        verify(notificationClient, times(1)).upsertNotificationPreference(any(UpsertNotificationSettings.class));

    }

    @Test
    void givenNoConnectionWithNotificationClient_whenSaveNotificationPreference_thenErrorIsLogged() {

        // Create a custom appender
        Logger logger = (Logger) LoggerFactory.getLogger(NotificationService.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        logger.addAppender(listAppender);
        listAppender.start();

        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@email.com";
        UpsertNotificationSettings notificationSettings = UpsertNotificationSettings.builder()
                .userId(userId)
                .contactInfo(email)
                .type("EMAIL")
                .notificationEnabled(true)
                .build();
        ResponseEntity<Void> mockResponse;
        mockResponse = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(notificationClient.upsertNotificationPreference(any(UpsertNotificationSettings.class))).thenReturn(mockResponse);

        // When
        notificationService.saveNotificationPreference(userId, true, email);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertFalse(logs.isEmpty());
        assertTrue(logs.get(0).getFormattedMessage().contains("[Feign call to notification-svc failed] Can't save user preference for user with id = [%s]".formatted(userId)));
        verify(notificationClient, times(1)).upsertNotificationPreference(any(UpsertNotificationSettings.class));

    }

    @Test
    void happyPath_whenGetNotificationSettings() {

        // Given
        UUID userId = UUID.randomUUID();
        ResponseEntity<NotificationSettings> mockResponse;
        mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(notificationClient.getUserNotificationSettings(userId)).thenReturn(mockResponse);

        // When
        NotificationSettings result = notificationService.getNotificationSettings(userId);

        // Then
        verify(notificationClient, times(1)).getUserNotificationSettings(userId);
    }

    @Test
    void whenMissingNotificationInDatabase_whenGetNotificationSettings_thenThrowException() {

        // Given
        UUID userId = UUID.randomUUID();
        ResponseEntity<NotificationSettings> mockResponse;
        mockResponse = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(notificationClient.getUserNotificationSettings(userId)).thenReturn(mockResponse);

        // When && Then
        assertThrows(RuntimeException.class, () -> notificationService.getNotificationSettings(userId));
        verify(notificationClient, times(1)).getUserNotificationSettings(userId);
    }


    @Test
    void happyPath_whenGetNotificationHistory() {

        // Given
        UUID userId = UUID.randomUUID();
        Notification notification1 = new Notification();
        notification1.setId(1); // Replace with actual fields
        notification1.setSubject("Notification 1");

        Notification notification2 = new Notification();
        notification2.setId(2); // Replace with actual fields
        notification2.setSubject("Notification 2");

        List<Notification> notificationList = List.of(notification1, notification2);
        ResponseEntity<List<Notification>> mockResponseList =
                new ResponseEntity<>(notificationList, HttpStatus.OK);

        when(notificationClient.getNotificationHistory(userId)).thenReturn(mockResponseList);

        // When
        List<Notification> notificationHistory = notificationService.getNotificationHistory(userId);

        // Then
        assertNotNull(notificationHistory);
        assertEquals(2, notificationHistory.size());
        verify(notificationClient, times(1)).getNotificationHistory(userId);
    }

    @Test
    void happyPath_whenSendNotification() {

        // Given
        UUID userId = UUID.randomUUID();
        String emailSubject = "Email Subject";
        String emailBody = "Email Body";

        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(userId)
                .subject(emailSubject)
                .body(emailBody)
                .build();

        ResponseEntity<Void> mockResponse;
        mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(notificationClient.sendNotification(notificationRequest)).thenReturn(mockResponse);

        // When
        notificationService.sendNotification(userId, emailSubject, emailBody);

        // Then
        verify(notificationClient, times(1)).sendNotification(notificationRequest);
    }

    @Test
    void whenNoConnectionWithNotificationClient_whenSendNotification_thenErrorIsLogged() {

        // Create a custom appender
        Logger logger = (Logger) LoggerFactory.getLogger(NotificationService.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        logger.addAppender(listAppender);
        listAppender.start();

        // Given
        UUID userId = UUID.randomUUID();
        String emailSubject = "Email Subject";
        String emailBody = "Email Body";

        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(userId)
                .subject(emailSubject)
                .body(emailBody)
                .build();

        ResponseEntity<Void> mockResponse;
        mockResponse = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(notificationClient.sendNotification(notificationRequest)).thenReturn(mockResponse);

        // When
        notificationService.sendNotification(userId, emailSubject, emailBody);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertFalse(logs.isEmpty());
        assertTrue(logs.get(0).getFormattedMessage().contains("[Feign call to notification-svc failed] Can't send email to user with id = [%s]".formatted(userId)));
        verify(notificationClient, times(1)).sendNotification(notificationRequest);
    }

    @Test
    void whenNoConnectionWithNotificationClient_whenSendNotification_thenThrowException() {

        // Create a custom appender
        Logger logger = (Logger) LoggerFactory.getLogger(NotificationService.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        logger.addAppender(listAppender);
        listAppender.start();

        // Given
        UUID userId = UUID.randomUUID();
        String emailSubject = "Email Subject";
        String emailBody = "Email Body";
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(userId)
                .subject(emailSubject)
                .body(emailBody)
                .build();

        when(notificationClient.sendNotification(notificationRequest)).thenThrow(new RuntimeException("mock Exception"));

        // When
        notificationService.sendNotification(userId, emailSubject, emailBody);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertFalse(logs.isEmpty());
        assertTrue(logs.get(0).getFormattedMessage().contains("Can't send email to user with id = [%s] due to 500 Internal Server Error.".formatted(userId)));
        verify(notificationClient, times(1)).sendNotification(notificationRequest);
    }


    @Test
    void happyPath_whenUpdateNotificationSettings() {

        // Given
        UUID userId = UUID.randomUUID();
        boolean enabled = true;
        ResponseEntity<Void> mockResponse;
        mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(notificationClient.updateNotificationSettings(userId, enabled)).thenReturn(mockResponse);
        //when(notificationClient.updateNotificationSettings(userId, enabled)).thenThrow(new RuntimeException("mock Exception"));

        // When
        notificationService.updateNotificationSettings(userId, enabled);

        // Then
        verify(notificationClient, times(1)).updateNotificationSettings(userId, enabled);
    }

    @Test
    void whenNoConnectionWithNotificationClient_whenUpdateNotificationSettings_thenThrowException() {

        // Create a custom appender
        Logger logger = (Logger) LoggerFactory.getLogger(NotificationService.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        logger.addAppender(listAppender);
        listAppender.start();

        // Given
        UUID userId = UUID.randomUUID();
        boolean enabled = true;
        ResponseEntity<Void> mockResponse;
        mockResponse = new ResponseEntity<>(HttpStatus.OK);
        //when(notificationClient.updateNotificationSettings(userId, enabled)).thenReturn(mockResponse);
        when(notificationClient.updateNotificationSettings(userId, enabled)).thenThrow(new RuntimeException("mock Exception"));

        // When
        notificationService.updateNotificationSettings(userId, enabled);

        // Then
        List<ILoggingEvent> logs = listAppender.list;
        assertFalse(logs.isEmpty());
        assertTrue(logs.get(0).getFormattedMessage().contains("Can't update notification preferences for user with id = [%s].".formatted(userId)));
        verify(notificationClient, times(1)).updateNotificationSettings(userId, enabled);
    }

    @Test
    void happyPath_whenClearHistory() {

        // Given
        UUID userId = UUID.randomUUID();
        ResponseEntity<Void> mockResponse;
        mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(notificationClient.clearHistory(userId)).thenReturn(mockResponse);
        //when(notificationClient.clearHistory(userId)).thenThrow(new RuntimeException("mock Exception"));

        // When
        notificationService.clearHistory(userId);

        // Then
        verify(notificationClient, times(1)).clearHistory(userId);
    }

    @Test
    void whenNoConnectionWithNotificationClient_whenClearHistory_thenThrowException() {

        // Create a custom appender
        Logger logger = (Logger) LoggerFactory.getLogger(NotificationService.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        logger.addAppender(listAppender);
        listAppender.start();

        // Given
        UUID userId = UUID.randomUUID();
        ResponseEntity<Void> mockResponse;
        mockResponse = new ResponseEntity<>(HttpStatus.OK);
        //when(notificationClient.clearHistory(userId, enabled)).thenReturn(mockResponse);
        when(notificationClient.clearHistory(userId)).thenThrow(new RuntimeException("mock Exception"));

        // When && Then
        assertThrows(FeignCallException.class, () -> notificationService.clearHistory(userId));
        List<ILoggingEvent> logs = listAppender.list;
        assertFalse(logs.isEmpty());
        assertTrue(logs.get(0).getFormattedMessage().contains("Unable to call notification-svc for clear notification history."));
        verify(notificationClient, times(1)).clearHistory(userId);
    }

    @Test
    void happyPath_whenRetryFailed() {

        // Given
        UUID userId = UUID.randomUUID();
        ResponseEntity<Void> mockResponse;
        mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(notificationClient.retryFailedNotifications(userId)).thenReturn(mockResponse);
        //when(notificationClient.retryFailedNotifications(userId)).thenThrow(new RuntimeException("mock Exception"));

        // When
        notificationService.retryFailed(userId);

        // Then
        verify(notificationClient, times(1)).retryFailedNotifications(userId);
    }

    @Test
    void whenNoConnectionWithNotificationClient_whenRetryFailed_thenThrowException() {

        // Create a custom appender
        Logger logger = (Logger) LoggerFactory.getLogger(NotificationService.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        logger.addAppender(listAppender);
        listAppender.start();

        // Given
        UUID userId = UUID.randomUUID();
        ResponseEntity<Void> mockResponse;
        mockResponse = new ResponseEntity<>(HttpStatus.OK);
        //when(notificationClient.retryFailedNotifications(userId, enabled)).thenReturn(mockResponse);
        when(notificationClient.retryFailedNotifications(userId)).thenThrow(new RuntimeException("mock Exception"));

        // When && Then
        assertThrows(FeignCallException.class, () -> notificationService.retryFailed(userId));
        List<ILoggingEvent> logs = listAppender.list;
        assertFalse(logs.isEmpty());
        assertTrue(logs.get(0).getFormattedMessage().contains("Unable to call notification-svc for re-send of failed notifications."));
        verify(notificationClient, times(1)).retryFailedNotifications(userId);
    }
}

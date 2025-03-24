package com.softuni.personal_finance_app.web;

import com.softuni.personal_finance_app.enitity.Invitation;
import com.softuni.personal_finance_app.enitity.Role;
import com.softuni.personal_finance_app.security.AuthenticatedUserDetails;
import com.softuni.personal_finance_app.service.NotificationService;
import com.softuni.personal_finance_app.service.UserService;
import com.softuni.personal_finance_app.web.dto.NotificationSettings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static com.softuni.personal_finance_app.TestBuilder.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@WebMvcTest(NotificationController.class)
public class NotificationControllerApiTest {

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void putRetryFailedNotifications_shouldRedirectToNotifications() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);
        MockHttpServletRequestBuilder request = put("/notifications")
                .with(user(principal))
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));
        verify(notificationService, times(1)).retryFailed(any());
    }

    @Test
    void deleteNotificationHistory_shouldRedirectToNotifications() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);
        MockHttpServletRequestBuilder request = delete("/notifications")
                .with(user(principal))
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));
        verify(notificationService, times(1)).clearHistory(any());
    }


    @Test
    void putUpdateNotificationSettings_shouldRedirectToNotifications() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);
        MockHttpServletRequestBuilder request = put("/notifications/user-settings")
                .param("enabled", String.valueOf(true))
                .with(user(principal))
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));
        verify(notificationService, times(1)).updateNotificationSettings(userId, true);
    }

    @Test
    void getRequestToSettingsEndpoint_shouldReturnSettingsView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);
        List<Invitation> invitationList = List.of(
                Invitation.builder()
                        .name("Invitation 1")
                        .build(),
                Invitation.builder()
                        .name("Invitation 1")
                        .build());

        MockHttpServletRequestBuilder request = get("/notifications")
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(aRandomUser());
        when(notificationService.getNotificationSettings(any())).thenReturn(NotificationSettings.builder().build());
        when(notificationService.getNotificationHistory(any())).thenReturn(List.of(aRandomNotification(), aRandomNotification()));

        //.andExpect() - проверявам резултата
        // MockMvcResultMatchers.status() - проверка на статуса
        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("notifications-page"))
                .andExpect(model().attributeExists("user", "notificationSettings", "succeededNotificationsNumber", "failedNotificationsNumber", "notificationHistory"));
        verify(userService, times(1)).getById(any());
        verify(notificationService, times(1)).getNotificationSettings(any());
        verify(notificationService, times(1)).getNotificationHistory(any());
    }
}

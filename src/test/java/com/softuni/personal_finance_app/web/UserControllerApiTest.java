package com.softuni.personal_finance_app.web;

import com.softuni.personal_finance_app.enitity.Invitation;
import com.softuni.personal_finance_app.enitity.Role;
import com.softuni.personal_finance_app.security.AuthenticatedUserDetails;
import com.softuni.personal_finance_app.service.BudgetService;
import com.softuni.personal_finance_app.service.InvitationService;
import com.softuni.personal_finance_app.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static com.softuni.personal_finance_app.TestBuilder.aRandomUser;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerApiTest {

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private InvitationService invitationService;
    @MockitoBean
    private BudgetService budgetService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void putUnauthorizedRequestToSwitchRole_shouldReturn404AndNotFoundView() throws Exception {

        // 1. Build Request
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(UUID.randomUUID(), "User123", "123123", Role.USER, true);
        MockHttpServletRequestBuilder request = put("/settings/{id}/role", UUID.randomUUID())
                .with(user(principal))
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(view().name("not-found"));
        verify(userService, never()).switchRole(any());
    }

    @Test
    void putAuthorizedRequestToSwitchRole_shouldRedirectToSettingsAdmin() throws Exception {

        // 1. Build Request
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(UUID.randomUUID(), "User123", "123123", Role.ADMIN, true);
        MockHttpServletRequestBuilder request = put("/settings/{id}/role", UUID.randomUUID())
                .with(user(principal))
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/admin"));
        verify(userService, times(1)).switchRole(any());
    }

    @Test
    void putUnauthorizedRequestToSwitchStatus_shouldReturn404AndNotFoundView() throws Exception {

        // 1. Build Request
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(UUID.randomUUID(), "User123", "123123", Role.USER, true);
        MockHttpServletRequestBuilder request = put("/settings/{id}/status", UUID.randomUUID())
                .with(user(principal))
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(view().name("not-found"));
        verify(userService, never()).switchStatus(any());
    }

    @Test
    void putAuthorizedRequestToSwitchStatus_shouldRedirectToSettingsAdmin() throws Exception {

        // 1. Build Request
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(UUID.randomUUID(), "User123", "123123", Role.ADMIN, true);
        MockHttpServletRequestBuilder request = put("/settings/{id}/status", UUID.randomUUID())
                .with(user(principal))
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/admin"));
        verify(userService, times(1)).switchStatus(any());
    }

    @Test
    void putProcessClientEditRequest_happyPath() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        //ClientEditRequest clientEditRequest = aRandomClientEditRequest();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);
        MockHttpServletRequestBuilder request = put("/settings")
                .formField("firstName", "nameFirst")
                .formField("lastName", "nameLast")
                .formField("email", "email@email.com")
                .with(user(principal))
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
        verify(userService, times(1)).editClientDetails(any(), any());
    }

    @Test
    void putProcessClientEditRequestWhenBindingError_thenRedirectSettings() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        //ClientEditRequest clientEditRequest = aRandomClientEditRequest();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);
        MockHttpServletRequestBuilder request = put("/settings")
                .formField("firstName", "nameFirst")
                .formField("lastName", "nameLast")
                .formField("email", "email")
                .with(user(principal))
                .with(csrf());
        when(userService.getById(userId)).thenReturn(aRandomUser());


        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("settings-page"))
                .andExpect(model().attributeExists("user", "clientEditRequest"));
        verify(userService, never()).editClientDetails(any(), any());
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

        MockHttpServletRequestBuilder request = get("/settings")
                .with(user(principal))
                .with(csrf());

        when(userService.getById(userId)).thenReturn(aRandomUser());
        when(invitationService.getSentInvitations(userId)).thenReturn(invitationList);

        //.andExpect() - проверявам резултата
        // MockMvcResultMatchers.status() - проверка на статуса
        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("settings-page"))
                .andExpect(model().attributeExists("user", "clientEditRequest", "invites", "activePage"));
    }

    @Test
    void getResendInvitation_shouldRedirectToInvites() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        UUID inviteId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);
        MockHttpServletRequestBuilder request = get("/settings/resend")
                .param("inviteId", String.valueOf(inviteId))
                .with(user(principal))
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/invites"));
        verify(invitationService, times(1)).resend(any());
    }

    @Test
    void getDeclineInvitation_shouldRedirectToInvites() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        UUID inviteId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);
        MockHttpServletRequestBuilder request = get("/settings/decline")
                .param("inviteId", String.valueOf(inviteId))
                .with(user(principal))
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/invites"));
        verify(invitationService, times(1)).decline(any());
    }

    @Test
    void getAcceptInvitation_shouldRedirectToInvites() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        UUID inviteId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);
        MockHttpServletRequestBuilder request = get("/settings/accept")
                .param("inviteId", String.valueOf(inviteId))
                .with(user(principal))
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/invites"));
        verify(invitationService, times(1)).accept(any());
    }

    @Test
    void getGetAllInvitationsEndpoint_shouldReturnInvitesView() throws Exception {

        // 1. Build Request
        UUID userId = UUID.randomUUID();
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(userId, "User123", "123123", Role.USER, true);

        MockHttpServletRequestBuilder request = get("/settings/invites")
                .with(user(principal))
                .with(csrf());

        List<Invitation> invitationList = List.of(
                Invitation.builder()
                        .name("Invitation 1")
                        .build(),
                Invitation.builder()
                        .name("Invitation 1")
                        .build());

        when(userService.getById(userId)).thenReturn(aRandomUser());
        when(invitationService.getReceivedInvitations(userId)).thenReturn(invitationList);
        when(invitationService.getSentInvitations(userId)).thenReturn(invitationList);

        //.andExpect() - проверявам резултата
        // MockMvcResultMatchers.status() - проверка на статуса
        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("invites-page"))
                .andExpect(model().attributeExists("user", "sentInvitations", "receivedInvitations"));
        verify(invitationService, times(1)).getReceivedInvitations(any());
        verify(invitationService, times(1)).getSentInvitations(any());

    }

    @Test
    void getUnauthorizedRequestToGetAllUsers_shouldReturn404AndNotFoundView() throws Exception {

        // 1. Build Request
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(UUID.randomUUID(), "User123", "123123", Role.USER, true);
        MockHttpServletRequestBuilder request = get("/settings/admin")
                .with(user(principal))
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(view().name("not-found"));
        verify(userService, never()).switchRole(any());
    }

    @Test
    void getAuthorizedRequestToGetAllUsers_shouldRedirectToSettingsAdmin() throws Exception {

        // 1. Build Request
        AuthenticatedUserDetails principal = new AuthenticatedUserDetails(UUID.randomUUID(), "User123", "123123", Role.ADMIN, true);
        MockHttpServletRequestBuilder request = get("/settings/admin")
                .with(user(principal))
                .with(csrf());

        when(userService.getAllUsers()).thenReturn(List.of(aRandomUser(), aRandomUser()));

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("admin-page"))
                .andExpect(model().attributeExists("users"));
        verify(userService, times(1)).getAllUsers();
    }
}

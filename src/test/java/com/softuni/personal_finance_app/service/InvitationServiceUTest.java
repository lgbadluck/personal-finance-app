package com.softuni.personal_finance_app.service;

import com.softuni.personal_finance_app.enitity.Invitation;
import com.softuni.personal_finance_app.enitity.InvitationStatus;
import com.softuni.personal_finance_app.exception.DomainException;
import com.softuni.personal_finance_app.repository.InvitationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InvitationServiceUTest {

    @Mock
    private  InvitationRepository invitationRepository;

    @InjectMocks
    private InvitationService invitationService;

    @Test
    void givenMissingInvitationInDatabase_whenAcceptInvitation_thenExceptionIsThrown() {

        // Given
        Invitation invitation = Invitation.builder()
                .id(UUID.randomUUID())
                .build();

        when(invitationRepository.findById(invitation.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DomainException.class, () -> invitationService.accept(invitation.getId()));
    }

    @Test
    void happyPath_whenAcceptInvitation() {

        // Given
        Invitation invitation = Invitation.builder()
                .id(UUID.randomUUID())
                .status(InvitationStatus.SENT)
                .build();
        when(invitationRepository.findById(invitation.getId())).thenReturn(Optional.of(invitation));

        // When
        invitationService.accept(invitation.getId());

        // Then
        assertEquals(InvitationStatus.ACCEPTED, invitation.getStatus());
        verify(invitationRepository, times(1)).save(invitation);
    }

    @Test
    void givenMissingInvitationInDatabase_whenDeclineInvitation_thenExceptionIsThrown() {

        // Given
        Invitation invitation = Invitation.builder()
                .id(UUID.randomUUID())
                .build();

        when(invitationRepository.findById(invitation.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DomainException.class, () -> invitationService.accept(invitation.getId()));
    }

    @Test
    void happyPath_whenDeclineInvitation() {

        // Given
        Invitation invitation = Invitation.builder()
                .id(UUID.randomUUID())
                .status(InvitationStatus.SENT)
                .build();
        when(invitationRepository.findById(invitation.getId())).thenReturn(Optional.of(invitation));

        // When
        invitationService.decline(invitation.getId());

        // Then
        assertEquals(InvitationStatus.DECLINED, invitation.getStatus());
        verify(invitationRepository, times(1)).save(invitation);
    }

    @Test
    void givenMissingInvitationInDatabase_whenResendInvitation_thenExceptionIsThrown() {

        // Given
        Invitation invitation = Invitation.builder()
                .id(UUID.randomUUID())
                .wasResend(false)
                .build();

        when(invitationRepository.findById(invitation.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DomainException.class, () -> invitationService.resend(invitation.getId()));
    }

    @Test
    void happyPath_whenResendInvitation() {

        // Given
        Invitation invitation = Invitation.builder()
                .id(UUID.randomUUID())
                .status(InvitationStatus.SENT)
                .wasResend(false)
                .build();
        when(invitationRepository.findById(invitation.getId())).thenReturn(Optional.of(invitation));

        // When
        invitationService.resend(invitation.getId());

        // Then
        assertTrue(invitation.isWasResend());
        verify(invitationRepository, times(2)).save(any(Invitation.class));
    }

    @Test
    void whenExistingSentInvitations_thenGetAllByUserId() {

        // Given
        UUID senderId = UUID.randomUUID();
        Invitation invitation1 = Invitation.builder()
                .id(UUID.randomUUID())
                .senderId(senderId)
                .status(InvitationStatus.SENT)
                .build();
        Invitation invitation2 = Invitation.builder()
                .id(UUID.randomUUID())
                .senderId(UUID.randomUUID())
                .status(InvitationStatus.SENT)
                .build();
        when(invitationRepository.findBySenderIdOrderByCreatedOnDesc(senderId)).thenReturn(List.of(invitation1));


        //When
        List<Invitation> sentInvitations = invitationService.getSentInvitations(senderId);

        // Then
        assertTrue(sentInvitations.contains(invitation1));
        assertThat(sentInvitations).hasSize(1);
    }

    @Test
    void whenExistingReceivedInvitations_thenGetAllByUserId() {

        // Given
        UUID receiverId = UUID.randomUUID();
        Invitation invitation1 = Invitation.builder()
                .id(UUID.randomUUID())
                .receiverId(receiverId)
                .status(InvitationStatus.SENT)
                .build();
        Invitation invitation2 = Invitation.builder()
                .id(UUID.randomUUID())
                .receiverId(UUID.randomUUID())
                .status(InvitationStatus.SENT)
                .build();
        when(invitationRepository.findByReceiverIdOrderByCreatedOnDesc(receiverId)).thenReturn(List.of(invitation1));


        //When
        List<Invitation> receivedInvitations = invitationService.getReceivedInvitations(receiverId);

        // Then
        assertTrue(receivedInvitations.contains(invitation1));
        assertThat(receivedInvitations).hasSize(1);
    }
}

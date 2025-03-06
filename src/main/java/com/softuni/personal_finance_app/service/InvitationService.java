package com.softuni.personal_finance_app.service;

import com.softuni.personal_finance_app.enitity.*;
import com.softuni.personal_finance_app.exception.DomainException;
import com.softuni.personal_finance_app.repository.InvitationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class InvitationService {


    private final InvitationRepository invitationRepository;

    @Autowired
    public InvitationService(InvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }


    public List<Invitation> getSentInvitations(UUID userId) {

        return invitationRepository.findBySenderIdOrderByCreatedOnDesc(userId);
    }

    public List<Invitation> getReceivedInvitations(UUID userId) {

        return invitationRepository.findByReceiverIdOrderByCreatedOnDesc(userId);
    }

    public Invitation accept(UUID inviteId) {

        Invitation invitation = invitationRepository.findById(inviteId)
                .orElseThrow(() -> new DomainException("No invitation found with id: [%s]".formatted(inviteId)));

        invitation.setStatus(InvitationStatus.ACCEPTED);

        invitationRepository.save(invitation);

        return invitation;
    }

    public Invitation decline(UUID inviteId) {

        Invitation invitation = invitationRepository.findById(inviteId)
                .orElseThrow(() -> new DomainException("No invitation found with id: [%s]".formatted(inviteId)));

        invitation.setStatus(InvitationStatus.DECLINED);

        invitationRepository.save(invitation);

        return invitation;
    }

    @Transactional
    public Invitation resend(UUID inviteId) {

        Invitation invitation = invitationRepository.findById(inviteId)
                .orElseThrow(() -> new DomainException("No invitation found with id: [%s]".formatted(inviteId)));

        invitation.setWasResend(true);

        invitationRepository.save(invitation);

        return invitationRepository.save(Invitation.builder()
                .name(invitation.getName())
                .budgetId(invitation.getBudgetId())
                .senderId(invitation.getSenderId())
                .senderUserName(invitation.getSenderUserName())
                .receiverId(invitation.getReceiverId())
                .receiverUserName(invitation.getReceiverUserName())
                .status(InvitationStatus.SENT)
                .build());
    }
}

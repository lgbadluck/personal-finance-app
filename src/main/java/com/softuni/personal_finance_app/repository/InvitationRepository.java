package com.softuni.personal_finance_app.repository;

import com.softuni.personal_finance_app.enitity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {
    List<Invitation> findBySenderId(UUID userId);
    List<Invitation> findByReceiverId(UUID userId);
}

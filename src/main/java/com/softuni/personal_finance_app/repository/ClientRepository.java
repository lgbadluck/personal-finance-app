package com.softuni.personal_finance_app.repository;

import com.softuni.personal_finance_app.enitity.Client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findByFirstName(String firstName);
}

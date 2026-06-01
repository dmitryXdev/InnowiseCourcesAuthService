package com.innowise.authservice.dao;

import com.innowise.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    @Query("select u from User u where u.login = :login")
    Optional<User> findByLogin(@Param("login") String login);

    @Query("select u from User u where u.id =:id")
    Optional<User> findById(@Param("id") Long id);
}

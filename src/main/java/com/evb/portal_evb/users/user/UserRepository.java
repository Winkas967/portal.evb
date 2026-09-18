package com.evb.portal_evb.users.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByNameIgnoreCase(String name);
    Optional<User> findByName(String name);
}

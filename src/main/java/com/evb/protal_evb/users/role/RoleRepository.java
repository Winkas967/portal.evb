package com.evb.protal_evb.users.role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    List<Role> findByUserIdAndIsActiveTrue(Integer userId);
}

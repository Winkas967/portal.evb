package com.evb.protal_evb.users.role;

import com.evb.protal_evb.users.role.dto.RoleRequest;
import com.evb.protal_evb.users.role.dto.RoleResponse;
import com.evb.protal_evb.users.user.User;
import com.evb.protal_evb.users.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public RoleService(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> findAll() {
        return roleRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public RoleResponse create(RoleRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Não foi encontrado usuário com o ID informado."));

        if (!user.isActive()) {
            throw new IllegalArgumentException("Não é possível atribuir um papel a um usuário inativo.");
        }

        Role role = new Role();
        role.setName(request.name().trim());
        role.setRole(request.role().trim());
        role.setUser(user);

        return toResponse(roleRepository.save(role));
    }

    @Transactional
    public RoleResponse deactivate(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Não foi encontrado papel com o ID informado."));

        if (!role.isActive()) {
            throw new IllegalArgumentException("O papel já está inativo.");
        }

        role.setActive(false);
        return toResponse(role);
    }

    @Transactional
    public RoleResponse reactivate(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Não foi encontrado papel com o ID informado."));

        if (role.isActive()) {
            throw new IllegalArgumentException("O papel já está ativo.");
        }

        role.setActive(true);
        return toResponse(role);
    }

    private RoleResponse toResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getRole(),
                role.getUser().getId(),
                role.getUser().getName()
                role.isActive()
        );
    }
}

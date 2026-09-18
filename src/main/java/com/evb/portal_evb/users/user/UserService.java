package com.evb.portal_evb.users.user;

import com.evb.portal_evb.audit.AuditLogService;
import com.evb.portal_evb.users.user.dto.UserRequest;
import com.evb.portal_evb.users.user.dto.UserResponse;
import com.evb.portal_evb.users.user.dto.UserUpdateRequest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private static final String ENTITY_TYPE = "USER";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        String name = request.name().trim();

        if (userRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Já existe um usuário com este nome.");
        }

        User user = new User();
        user.setName(name);
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        User saved = userRepository.save(user);
        auditLogService.record(ENTITY_TYPE, saved.getId(), "CREATE", "Usuário cadastrado: " + saved.getName());
        return toResponse(saved);
    }

    @Transactional
    public UserResponse deactivate(Integer id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Não for encontrado usuário com ID informado."));

        if (!user.isActive()) {
            throw new IllegalArgumentException("O usuário já está inativo.");
        }

        user.setActive(false);
        auditLogService.record(ENTITY_TYPE, user.getId(), "DEACTIVATE", "Usuário excluído (desativado): " + user.getName());
        return toResponse(user);
    }

    @Transactional
    public UserResponse reactivate(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Não foi encontrado nenhum usuário com o ID informado."));

        if(user.isActive()) {
            throw new IllegalArgumentException("O usuário já está ativo.");
        }

        user.setActive(true);
        auditLogService.record(ENTITY_TYPE, user.getId(), "REACTIVATE", "Usuário reativado: " + user.getName());
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.isActive());
    }

    @Transactional
    public UserResponse update(Integer id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Não foi encontrado usuário com esse ID"));

        if (!user.isActive()) {
            throw new IllegalArgumentException(("Não é possível editar um usuário inativo."));
        }

        if (request.name() != null) {
            String name = request.name().trim();

            if (name.isEmpty()) {
                throw new IllegalArgumentException("Preencha o campo de nome.");
            }
            if (!name.equalsIgnoreCase(user.getName()) && userRepository.existsByNameIgnoreCase(name)) {
                throw new IllegalArgumentException("Já existe um usuário com esse nome.");
            }

            user.setName(name);
        }
        if (request.password() != null) {
            user.setPasswordHash((passwordEncoder.encode(request.password())));
        }

        auditLogService.record(ENTITY_TYPE, user.getId(), "UPDATE", "Usuário atualizado: " + user.getName());
        return toResponse(user);
    }
}

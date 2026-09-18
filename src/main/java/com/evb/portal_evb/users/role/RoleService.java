package com.evb.portal_evb.users.role;

import com.evb.portal_evb.audit.AuditLogService;
import com.evb.portal_evb.users.role.dto.RoleRequest;
import com.evb.portal_evb.users.role.dto.RoleResponse;
import com.evb.portal_evb.users.role.dto.RoleUpdateRequest;
import com.evb.portal_evb.users.user.User;
import com.evb.portal_evb.users.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleService {

    private static final String ENTITY_TYPE = "ROLE";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public RoleService(RoleRepository roleRepository, UserRepository userRepository, AuditLogService auditLogService) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
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

        Role saved = roleRepository.save(role);
        auditLogService.record(ENTITY_TYPE, saved.getId(), "CREATE", "Papel cadastrado: " + saved.getRole() + " para " + user.getName());
        return toResponse(saved);
    }

    @Transactional
    public RoleResponse update(Integer id, RoleUpdateRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Não foi encontrado papel com o ID informado."));

        if (!role.isActive()) {
            throw new IllegalArgumentException("Não é possível editar um papel inativo.");
        }

        if (request.name() != null) {
            String name = request.name().trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Preencha o campo de nome.");
            }
            role.setName(name);
        }
        if (request.role() != null) {
            String roleValue = request.role().trim();
            if (roleValue.isEmpty()) {
                throw new IllegalArgumentException("Preencha o campo de papel/permissão.");
            }
            role.setRole(roleValue);
        }

        auditLogService.record(ENTITY_TYPE, role.getId(), "UPDATE", "Papel atualizado: " + role.getRole());
        return toResponse(role);
    }

    @Transactional
    public RoleResponse deactivate(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Não foi encontrado papel com o ID informado."));

        if (!role.isActive()) {
            throw new IllegalArgumentException("O papel já está inativo.");
        }

        role.setActive(false);
        auditLogService.record(ENTITY_TYPE, role.getId(), "DEACTIVATE", "Papel excluído (desativado): " + role.getRole());
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
        auditLogService.record(ENTITY_TYPE, role.getId(), "REACTIVATE", "Papel reativado: " + role.getRole());
        return toResponse(role);
    }

    private RoleResponse toResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getRole(),
                role.getUser().getId(),
                role.getUser().getName(),
                role.isActive()
        );
    }
}

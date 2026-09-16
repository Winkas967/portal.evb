package com.evb.protal_evb.users.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse deactivate(Integer id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Não for encontrado usuário com ID informado."));

        if (!user.isActive()) {
            throw new IllegalArgumentException("O usuário já está inativo.");
        }

        user.setActive(false);
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
        return toResponse(user);
    }
}

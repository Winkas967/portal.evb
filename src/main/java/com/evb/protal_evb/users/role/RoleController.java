package com.evb.protal_evb.users.role;

import com.evb.protal_evb.users.role.dto.RoleRequest;
import com.evb.protal_evb.users.role.dto.RoleResponse;
import com.evb.protal_evb.users.role.dto.RoleUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public List<RoleResponse> findAll() {
        return roleService.findAll();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RoleResponse> create(@Valid @RequestBody RoleRequest request) {
        RoleResponse created = roleService.create(request);
        return ResponseEntity.created(URI.create("/api/roles/" + created.id())).body(created);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RoleResponse> update(@PathVariable Integer id, @Valid @RequestBody RoleUpdateRequest request) {
        return ResponseEntity.ok(roleService.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<RoleResponse> deactivate(@PathVariable Integer id) {
        return ResponseEntity.ok(roleService.deactivate(id));
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<RoleResponse> reactivate(@PathVariable Integer id) {
        return ResponseEntity.ok(roleService.reactivate(id));
    }
}

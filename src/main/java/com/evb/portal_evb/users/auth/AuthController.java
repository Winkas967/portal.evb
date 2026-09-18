package com.evb.portal_evb.users.auth;

import com.evb.portal_evb.security.JwtService;
import com.evb.portal_evb.security.UserPrincipal;
import com.evb.portal_evb.users.auth.dto.AuthRequest;
import com.evb.portal_evb.users.auth.dto.AuthResponse;
import com.evb.portal_evb.users.auth.dto.MeResponse;
import com.evb.portal_evb.users.role.Role;
import com.evb.portal_evb.users.role.RoleRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;


import java.time.Duration;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final boolean secureCookie;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, RoleRepository roleRepository, @Value("${app.security.cookie-secure}") boolean secureCookie) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
        this.secureCookie = secureCookie;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        String token = jwtService.generateToken(request.username());
        ResponseCookie cookie = accessTokenCookie(token, Duration.ofMillis(jwtService.getExpirationMillis()));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(token));
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.startsWith("ROLE_") ? authority.substring(5) : authority)
                .toList();

        String displayRole = "Sem papel definido";
        if (authentication.getPrincipal() instanceof UserPrincipal principal) {
            List<Role> activeRoles = roleRepository.findByUserIdAndIsActiveTrue(principal.getId());

            Optional<Role> adminRole = activeRoles.stream()
                    .filter(role -> "ADMIN".equals(role.getRole()))
                    .findFirst();

            if (adminRole.isPresent()) {
                displayRole = adminRole.get().getName();
            } else if (!activeRoles.isEmpty()) {
                displayRole = activeRoles.get(0).getName();
            }
        }

        return ResponseEntity.ok(new MeResponse(authentication.getName(), roles, displayRole));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = accessTokenCookie("", Duration.ZERO);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    private ResponseCookie accessTokenCookie(String value, Duration maxAge) {
        return ResponseCookie.from("access_token", value)
                .httpOnly(true).secure(secureCookie).sameSite("Lax").path("/").maxAge(maxAge).build();
    }
}

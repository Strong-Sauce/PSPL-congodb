package com.postSale.amcProject.Services;

import com.postSale.amcProject.Model.dto.auth.*;
import com.postSale.amcProject.Model.nodes.User;
import com.postSale.amcProject.Repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.postSale.amcProject.Model.nodes.Customer;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.time.LocalDateTime.now;

/**
 * AuthService contains all authentication business logic:
 * signup, login, forgot password, reset password, logout, and session restore.
 */
@Service
public class AuthService {

    // Reset tokens expire after 30 minutes
    private static final int RESET_TOKEN_EXPIRY_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Base URL used to build the password reset link (e.g. http://localhost:4200)
    @Value("${app.base-url:http://localhost:4200}")
    private String appBaseUrl;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ─── SIGNUP ────────────────────────────────────────────────────────────────

    /**
     * Creates a new user account, hashes their password, and logs them in.
     */
    @Transactional
    public AuthResponse signup(SignupRequest request, HttpServletRequest httpRequest) {
        String email = normalizeEmail(request.email());

        // Check if email is already taken
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        String name = request.name().trim();
        String passwordHash = passwordEncoder.encode(request.password());

        String userId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();

        LocalDateTime createdAt = now();

        userRepository.createUser(
                userId,
                name,
                email,
                passwordHash,
                createdAt,
                createdAt,
                customerId
        );

        User user = new User();
        user.setId(userId);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordHash);
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(createdAt);

        // Automatically log the user in after signup
        createSession(user, httpRequest);

        return new AuthResponse("Signup successful", toDto(user));
    }

    // ─── LOGIN ─────────────────────────────────��───────────────────────────────

    /**
     * Validates email + password and creates an authenticated session.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String email = normalizeEmail(request.email());

        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        // Verify the password against the stored BCrypt hash
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        // Create server-side session
        createSession(user, httpRequest);

        return new AuthResponse("Login successful", toDto(user));
    }

    // ─── CURRENT USER ──────────────────────────────────────────────────────────

    /**
     * Returns the currently logged-in user's data.
     * Spring Security injects the Authentication from the session automatically.
     */
    @Transactional(readOnly = true)
    public Optional<AuthUserResponse> currentUser(Authentication authentication) {
        // Check if the user is actually authenticated (not an anonymous guest)
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        String email = normalizeEmail(authentication.getName());
        return userRepository.findByEmail(email).map(this::toDto);
    }

    // ─── LOGOUT ───────────────────────────────────────────────────────────────

    /**
     * Destroys the server-side session and clears the security context.
     */
    public MessageResponse logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false); // false = don't create a new session
        if (session != null) {
            session.invalidate(); // Destroys the session and its data
        }
        SecurityContextHolder.clearContext();
        return new MessageResponse("Logout successful");
    }

    // ─── HELPERS ───────────────────────────────────────────────────────────────

    /**
     * Creates a Spring Security session for the given user.
     * This is what makes the browser "stay logged in" — the session ID is stored
     * in a cookie (JSESSIONID) and Spring reads it on every subsequent request.
     */
    private void createSession(User user, HttpServletRequest request) {
        // Create an authentication token (username = email, no credentials needed here)
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Put it in the security context
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // Persist the context in the HTTP session so Spring can restore it on the next request
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }

    /** Trims and lowercases an email address to avoid case-sensitivity issues. */
    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    /** Maps a User node to a safe DTO (no password included). */
    private AuthUserResponse toDto(User user) {
        return new AuthUserResponse(user.getId(), user.getName(), user.getEmail());
    }

    /** Generates a cryptographically secure, URL-safe random token. */
    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}


package com.sian.noteshare.service;

import com.sian.noteshare.dto.JwtAuthenticationResponse;
import com.sian.noteshare.dto.LoginRequest;
import com.sian.noteshare.dto.RegisterRequest;
import com.sian.noteshare.entity.User;
import com.sian.noteshare.exception.InvalidCredentialsException;
import com.sian.noteshare.exception.UserAlreadyExistsException;
import com.sian.noteshare.repository.UserRepository;
import com.sian.noteshare.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Service class responsible for user authentication and registration operations.
 * Handles the creation of new users, password hashing, and JWT token generation.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    /**
     * Authenticates a user based on email and password and generates a JWT.
     *
     * @param request The login request containing email and password.
     * @return JwtAuthenticationResponse containing the generated JWT token.
     * @throws InvalidCredentialsException if authentication fails.
     * @throws UsernameNotFoundException if the user does not exist.
     */
    public JwtAuthenticationResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail());

        return JwtAuthenticationResponse.builder()
                .accessToken(token)
                .build();
    }

    /**
     * Registers a new local user, hashes their password, and sends a welcome email.
     *
     * @param request The registration request containing username, email, and password.
     * @return JwtAuthenticationResponse containing the generated JWT token.
     * @throws UserAlreadyExistsException if the email or username is already taken.
     */
    public JwtAuthenticationResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email " + request.getEmail() + " is already registered.");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username " + request.getUsername() + " is already taken.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(User.AuthProvider.LOCAL)
                .roles(Set.of("ROLE_USER"))
                .build();

        userRepository.save(user);
        emailService.sendWelcomeEmail(user);

        return JwtAuthenticationResponse.builder()
                .accessToken(jwtUtil.generateToken(user.getEmail()))
                .build();
    }
}
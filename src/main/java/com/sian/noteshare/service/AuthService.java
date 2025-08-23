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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public JwtAuthenticationResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(),
                            request.getPassword())
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

    public JwtAuthenticationResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email " + request.getEmail() + " is already registered.");
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

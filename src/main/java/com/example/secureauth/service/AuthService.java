
package com.example.secureauth.service;

import com.example.secureauth.dto.ApiResponse;
import com.example.secureauth.dto.AuthResponse;
import com.example.secureauth.dto.LoginRequest;
import com.example.secureauth.dto.RegisterRequest;
import com.example.secureauth.entity.RevokedToken;
import com.example.secureauth.entity.User;
import com.example.secureauth.enums.Role;
import com.example.secureauth.repository.RevokedTokenRepository;
import com.example.secureauth.repository.UserRepository;
import com.example.secureauth.security.JwtService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    public AuthService(
            UserRepository userRepository,
            RevokedTokenRepository revokedTokenRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.revokedTokenRepository = revokedTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    // Registration
    public ApiResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        if (userRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("User ID already taken");
        }

        User user = new User();

        user.setName(request.getName());
        user.setUserId(request.getUserId());
        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(Role.USER);

        userRepository.save(user);

        return new ApiResponse(
                true,
                "Registration successful"
        );
    }

    // Login
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                jwtExpiration,
                user.getId(),
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    // Logout
    public ApiResponse logout(Jwt jwt) {

        String jti = jwt.getId();

        if (jti == null || jti.isBlank()) {
            throw new IllegalArgumentException(
                    "Invalid token ID"
            );
        }

        if (!revokedTokenRepository.existsByJti(jti)) {

            RevokedToken revokedToken = new RevokedToken(
                    jti,
                    jwt.getExpiresAt()
            );

            revokedTokenRepository.save(revokedToken);
        }

        return new ApiResponse(
                true,
                "Logout successful"
        );
    }
}

package com.cloudops.incidentmanager.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.cloudops.incidentmanager.dto.AuthRequest;
import com.cloudops.incidentmanager.dto.AuthResponse;
import com.cloudops.incidentmanager.repository.UserRepository;
import com.cloudops.incidentmanager.security.JwtProperties;
import com.cloudops.incidentmanager.security.JwtTokenProvider;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final JwtProperties jwtProperties;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtTokenProvider jwtTokenProvider,
            UserDetailsService userDetailsService,
            JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.jwtProperties = jwtProperties;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        return new AuthResponse(
                accessToken,
                refreshToken,
                jwtProperties.expirationMs() / 1000,
                new AuthResponse.UserInfo(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole().name())
        );
    }
}

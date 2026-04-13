package edu.tcu.cs.projectpulse.controller;

import edu.tcu.cs.projectpulse.dto.LoginRequest;
import edu.tcu.cs.projectpulse.dto.LoginResponse;
import edu.tcu.cs.projectpulse.dto.RegisterRequest;
import edu.tcu.cs.projectpulse.model.User;
import edu.tcu.cs.projectpulse.repository.UserRepository;
import edu.tcu.cs.projectpulse.security.JwtTokenProvider;
import edu.tcu.cs.projectpulse.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserService userService;

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        String token = jwtTokenProvider.generateToken(auth);
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        return ResponseEntity.ok(new LoginResponse(
                token, user.getRole().name(), user.getId(),
                user.getEmail(), user.getFirstName(), user.getLastName()));
    }

    @PostMapping("/register/student")
    public ResponseEntity<LoginResponse> registerStudent(@Valid @RequestBody RegisterRequest request) {
        User user = userService.registerUser(request, User.Role.STUDENT);
        String token = jwtTokenProvider.generateToken(user.getEmail());
        return ResponseEntity.ok(new LoginResponse(
                token, user.getRole().name(), user.getId(),
                user.getEmail(), user.getFirstName(), user.getLastName()));
    }

    @PostMapping("/register/instructor")
    public ResponseEntity<LoginResponse> registerInstructor(@Valid @RequestBody RegisterRequest request) {
        User user = userService.registerUser(request, User.Role.INSTRUCTOR);
        String token = jwtTokenProvider.generateToken(user.getEmail());
        return ResponseEntity.ok(new LoginResponse(
                token, user.getRole().name(), user.getId(),
                user.getEmail(), user.getFirstName(), user.getLastName()));
    }

    @GetMapping("/register/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        var inv = userService.validateToken(token);
        return ResponseEntity.ok(java.util.Map.of(
                "email", inv.getEmail(),
                "role", inv.getRole().name(),
                "sectionName", inv.getSection() != null ? inv.getSection().getName() : ""
        ));
    }
}

package edu.tcu.cs.projectpulse.config;

import edu.tcu.cs.projectpulse.model.User;
import edu.tcu.cs.projectpulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        ensureUser("admin@projectpulse.edu", "admin123", "System", "Admin", User.Role.ADMIN);
        ensureUser("b.wei@abc.edu", "123456", "Bingyang", "Wei", User.Role.INSTRUCTOR);
        ensureUser("j.smith@abc.edu", "123456", "John", "Smith", User.Role.STUDENT);
        ensureUser("s.johnson@abc.edu", "123456", "Sarah", "Johnson", User.Role.STUDENT);
    }

    private void ensureUser(String email, String rawPassword, String firstName, String lastName, User.Role role) {
        userRepository.findByEmail(email).ifPresentOrElse(
            existing -> {
                if (!existing.isEnabled()) {
                    existing.setEnabled(true);
                    userRepository.save(existing);
                    log.info("Re-enabled seed user: {}", email);
                }
            },
            () -> {
                User user = User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(rawPassword))
                        .firstName(firstName)
                        .lastName(lastName)
                        .role(role)
                        .enabled(true)
                        .build();
                userRepository.save(user);
                log.info("Created seed user: {} / {}", email, rawPassword);
            }
        );
    }
}

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
        ensureUser("m.garcia@abc.edu", "123456", "Maria", "Garcia", User.Role.STUDENT);
        ensureUser("t.nguyen@abc.edu", "123456", "Tommy", "Nguyen", User.Role.STUDENT);
        ensureUser("a.patel@abc.edu", "123456", "Anika", "Patel", User.Role.STUDENT);
    }

    private void ensureUser(String email, String rawPassword, String firstName, String lastName, User.Role role) {
        userRepository.findByEmail(email).ifPresentOrElse(
            existing -> {
                boolean changed = false;
                if (!existing.isEnabled()) {
                    existing.setEnabled(true);
                    changed = true;
                }
                if (!passwordEncoder.matches(rawPassword, existing.getPassword())) {
                    existing.setPassword(passwordEncoder.encode(rawPassword));
                    changed = true;
                    log.info("Reset password for seed user: {}", email);
                }
                if (changed) userRepository.save(existing);
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

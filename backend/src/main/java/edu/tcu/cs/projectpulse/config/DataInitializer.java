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
        // Create default admin
        if (!userRepository.existsByEmail("admin@projectpulse.edu")) {
            User admin = User.builder()
                    .email("admin@projectpulse.edu")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("System")
                    .lastName("Admin")
                    .role(User.Role.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("Default admin created: admin@projectpulse.edu / admin123");
        }

        // Create default instructor (from spec)
        if (!userRepository.existsByEmail("b.wei@abc.edu")) {
            User instructor = User.builder()
                    .email("b.wei@abc.edu")
                    .password(passwordEncoder.encode("123456"))
                    .firstName("Bingyang")
                    .lastName("Wei")
                    .role(User.Role.INSTRUCTOR)
                    .enabled(true)
                    .build();
            userRepository.save(instructor);
            log.info("Default instructor created: b.wei@abc.edu / 123456");
        }

        // Create default student (from spec)
        if (!userRepository.existsByEmail("j.smith@abc.edu")) {
            User student = User.builder()
                    .email("j.smith@abc.edu")
                    .password(passwordEncoder.encode("123456"))
                    .firstName("John")
                    .lastName("Smith")
                    .role(User.Role.STUDENT)
                    .enabled(true)
                    .build();
            userRepository.save(student);
            log.info("Default student created: j.smith@abc.edu / 123456");
        }
    }
}

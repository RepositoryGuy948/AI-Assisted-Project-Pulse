package edu.tcu.cs.projectpulse.service;

import edu.tcu.cs.projectpulse.dto.InviteRequest;
import edu.tcu.cs.projectpulse.dto.RegisterRequest;
import edu.tcu.cs.projectpulse.dto.UserDto;
import edu.tcu.cs.projectpulse.model.InvitationToken;
import edu.tcu.cs.projectpulse.model.Section;
import edu.tcu.cs.projectpulse.model.User;
import edu.tcu.cs.projectpulse.repository.InvitationTokenRepository;
import edu.tcu.cs.projectpulse.repository.SectionRepository;
import edu.tcu.cs.projectpulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private static final Set<String> PROTECTED_EMAILS = Set.of(
        "admin@projectpulse.edu", "b.wei@abc.edu", "j.smith@abc.edu", "s.johnson@abc.edu"
    );

    private final UserRepository userRepository;
    private final InvitationTokenRepository tokenRepository;
    private final SectionRepository sectionRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public int inviteUsers(Long sectionId, InviteRequest request, User.Role role) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        String adminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByEmail(adminEmail).orElse(null);
        String adminName = (admin != null)
                ? admin.getFirstName() + " " + admin.getLastName()
                : "An administrator";

        List<String> emails = Arrays.stream(request.getEmails().split(";"))
                .map(String::trim)
                .filter(e -> !e.isEmpty())
                .collect(Collectors.toList());

        for (String email : emails) {
            String token = UUID.randomUUID().toString();
            InvitationToken invToken = InvitationToken.builder()
                    .email(email)
                    .token(token)
                    .section(section)
                    .role(role)
                    .used(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            tokenRepository.save(invToken);
            if (role == User.Role.STUDENT) {
                emailService.sendStudentInvitation(email, token, section.getName(),
                        request.getCustomMessage(), adminName);
            } else {
                emailService.sendInstructorInvitation(email, token, section.getName(),
                        request.getCustomMessage(), adminName);
            }
        }
        return emails.size();
    }

    public User registerUser(RegisterRequest request, User.Role expectedRole) {
        InvitationToken token = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired token."));

        if (token.isUsed()) {
            throw new IllegalArgumentException("Token already used.");
        }
        if (token.getRole() != expectedRole) {
            throw new IllegalArgumentException("Invalid token for this role.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .middleInitial(request.getMiddleInitial())
                .lastName(request.getLastName())
                .role(expectedRole)
                .enabled(true)
                .build();

        userRepository.save(user);
        token.setUsed(true);
        tokenRepository.save(token);
        return user;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    public List<User> searchStudents(String firstName, String lastName, String email,
                                     Long teamId, Long sectionId,
                                     String teamName, String sectionName) {
        // UC-15 sort: section name descending, then last name ascending
        return userRepository.searchStudents(firstName, lastName, email, teamId, sectionId, teamName, sectionName)
                .stream()
                .sorted(Comparator
                        .comparing((User u) -> u.getTeam() != null && u.getTeam().getSection() != null
                                ? u.getTeam().getSection().getName() : "",
                                Comparator.reverseOrder())
                        .thenComparing(u -> u.getLastName().toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<User> searchInstructors(String firstName, String lastName, String email, Boolean enabled) {
        return userRepository.searchByRole(User.Role.INSTRUCTOR, firstName, lastName, email, enabled);
    }

    public User updateUser(Long id, UserDto dto) {
        User user = getUserById(id);
        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
        if (dto.getMiddleInitial() != null) user.setMiddleInitial(dto.getMiddleInitial());
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email already in use.");
            }
            user.setEmail(dto.getEmail());
        }
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    public void deactivateInstructor(Long id) {
        User instructor = getUserById(id);
        if (PROTECTED_EMAILS.contains(instructor.getEmail())) {
            throw new IllegalArgumentException("Demo accounts cannot be deactivated.");
        }
        instructor.setEnabled(false);
        userRepository.save(instructor);
    }

    public void reactivateInstructor(Long id) {
        User instructor = getUserById(id);
        instructor.setEnabled(true);
        userRepository.save(instructor);
    }

    public void deactivateStudent(Long id) {
        User student = getUserById(id);
        if (PROTECTED_EMAILS.contains(student.getEmail())) {
            throw new IllegalArgumentException("Demo accounts cannot be deactivated.");
        }
        student.setEnabled(false);
        userRepository.save(student);
    }

    public void reactivateStudent(Long id) {
        User student = getUserById(id);
        student.setEnabled(true);
        userRepository.save(student);
    }

    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setMiddleInitial(user.getMiddleInitial());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole().name());
        dto.setEnabled(user.isEnabled());
        if (user.getRole() == User.Role.STUDENT && user.getTeam() != null) {
            dto.setTeamId(user.getTeam().getId());
            dto.setTeamName(user.getTeam().getName());
            if (user.getTeam().getSection() != null) {
                dto.setSectionId(user.getTeam().getSection().getId());
                dto.setSectionName(user.getTeam().getSection().getName());
            }
        }
        if (user.getRole() == User.Role.INSTRUCTOR && user.getInstructorTeams() != null) {
            dto.setSupervisedTeams(user.getInstructorTeams().stream()
                    .map(t -> new UserDto.TeamSummary(t.getId(), t.getName(),
                            t.getSection() != null ? t.getSection().getName() : ""))
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public InvitationToken validateToken(String token) {
        InvitationToken inv = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired registration link."));
        if (inv.isUsed()) {
            throw new IllegalArgumentException("This registration link has already been used.");
        }
        return inv;
    }
}

package edu.tcu.cs.projectpulse.controller;

import edu.tcu.cs.projectpulse.dto.UserDto;
import edu.tcu.cs.projectpulse.model.User;
import edu.tcu.cs.projectpulse.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // --- Students ---

    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<List<UserDto>> getStudents(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Long sectionId) {
        return ResponseEntity.ok(userService.searchStudents(firstName, lastName, email, teamId, sectionId)
                .stream().map(userService::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/students/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<UserDto> getStudent(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toDto(userService.getUserById(id)));
    }

    @PutMapping("/students/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<UserDto> updateStudent(@PathVariable Long id, @RequestBody UserDto dto) {
        return ResponseEntity.ok(userService.toDto(userService.updateUser(id, dto)));
    }

    @DeleteMapping("/students/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/students/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateStudent(@PathVariable Long id) {
        userService.deactivateStudent(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/students/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reactivateStudent(@PathVariable Long id) {
        userService.reactivateStudent(id);
        return ResponseEntity.ok().build();
    }

    // --- Instructors ---

    @GetMapping("/instructors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getInstructors(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean enabled) {
        return ResponseEntity.ok(userService.searchInstructors(firstName, lastName, email, enabled)
                .stream().map(userService::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/instructors/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<UserDto> getInstructor(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toDto(userService.getUserById(id)));
    }

    @PutMapping("/instructors/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<UserDto> updateInstructor(@PathVariable Long id, @RequestBody UserDto dto) {
        return ResponseEntity.ok(userService.toDto(userService.updateUser(id, dto)));
    }

    @PutMapping("/instructors/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateInstructor(@PathVariable Long id) {
        userService.deactivateInstructor(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/instructors/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reactivateInstructor(@PathVariable Long id) {
        userService.reactivateInstructor(id);
        return ResponseEntity.ok().build();
    }

    // --- Current user ---

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        User user = userService.getUserByEmail(authentication.getName());
        return ResponseEntity.ok(userService.toDto(user));
    }
}

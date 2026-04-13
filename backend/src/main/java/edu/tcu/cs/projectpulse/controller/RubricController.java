package edu.tcu.cs.projectpulse.controller;

import edu.tcu.cs.projectpulse.dto.RubricDto;
import edu.tcu.cs.projectpulse.service.RubricService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rubrics")
@RequiredArgsConstructor
public class RubricController {

    private final RubricService rubricService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RubricDto> createRubric(@Valid @RequestBody RubricDto dto) {
        return ResponseEntity.ok(rubricService.toDto(rubricService.createRubric(dto)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<List<RubricDto>> getAllRubrics() {
        return ResponseEntity.ok(rubricService.getAllRubrics().stream()
                .map(rubricService::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<RubricDto> getRubric(@PathVariable Long id) {
        return ResponseEntity.ok(rubricService.toDto(rubricService.getRubricById(id)));
    }
}

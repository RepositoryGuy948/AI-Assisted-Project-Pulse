package edu.tcu.cs.projectpulse.service;

import edu.tcu.cs.projectpulse.dto.RubricDto;
import edu.tcu.cs.projectpulse.model.Criterion;
import edu.tcu.cs.projectpulse.model.Rubric;
import edu.tcu.cs.projectpulse.repository.RubricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RubricService {

    private final RubricRepository rubricRepository;

    public Rubric createRubric(RubricDto dto) {
        if (rubricRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Rubric with name '" + dto.getName() + "' already exists.");
        }
        Rubric rubric = Rubric.builder().name(dto.getName()).build();
        if (dto.getCriteria() != null) {
            List<Criterion> criteria = dto.getCriteria().stream().map(c -> {
                if (c.getMaxScore() == null || c.getMaxScore() <= 0) {
                    throw new IllegalArgumentException("Max score must be positive for criterion: " + c.getName());
                }
                return Criterion.builder()
                        .name(c.getName())
                        .description(c.getDescription())
                        .maxScore(c.getMaxScore())
                        .rubric(rubric)
                        .build();
            }).collect(Collectors.toList());
            rubric.getCriteria().addAll(criteria);
        }
        return rubricRepository.save(rubric);
    }

    public List<Rubric> getAllRubrics() {
        return rubricRepository.findAll();
    }

    public Rubric getRubricById(Long id) {
        return rubricRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rubric not found: " + id));
    }

    public RubricDto toDto(Rubric rubric) {
        RubricDto dto = new RubricDto();
        dto.setId(rubric.getId());
        dto.setName(rubric.getName());
        dto.setCriteria(rubric.getCriteria().stream().map(c -> {
            RubricDto.CriterionDto cd = new RubricDto.CriterionDto();
            cd.setId(c.getId());
            cd.setName(c.getName());
            cd.setDescription(c.getDescription());
            cd.setMaxScore(c.getMaxScore());
            return cd;
        }).collect(Collectors.toList()));
        return dto;
    }
}

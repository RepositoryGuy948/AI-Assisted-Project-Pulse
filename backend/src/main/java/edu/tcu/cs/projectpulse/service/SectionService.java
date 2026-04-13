package edu.tcu.cs.projectpulse.service;

import edu.tcu.cs.projectpulse.dto.ActiveWeekDto;
import edu.tcu.cs.projectpulse.dto.SectionDto;
import edu.tcu.cs.projectpulse.model.ActiveWeek;
import edu.tcu.cs.projectpulse.model.Rubric;
import edu.tcu.cs.projectpulse.model.Section;
import edu.tcu.cs.projectpulse.repository.ActiveWeekRepository;
import edu.tcu.cs.projectpulse.repository.RubricRepository;
import edu.tcu.cs.projectpulse.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SectionService {

    private final SectionRepository sectionRepository;
    private final RubricRepository rubricRepository;
    private final ActiveWeekRepository activeWeekRepository;

    public Section createSection(SectionDto dto) {
        if (sectionRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Section '" + dto.getName() + "' already exists.");
        }
        Section section = new Section();
        section.setName(dto.getName());
        section.setStartDate(dto.getStartDate());
        section.setEndDate(dto.getEndDate());
        if (dto.getRubricId() != null) {
            Rubric rubric = rubricRepository.findById(dto.getRubricId())
                    .orElseThrow(() -> new RuntimeException("Rubric not found"));
            section.setRubric(rubric);
        }
        section = sectionRepository.save(section);
        generateActiveWeeks(section);
        return section;
    }

    public Section updateSection(Long id, SectionDto dto) {
        Section section = getById(id);
        if (!section.getName().equals(dto.getName()) && sectionRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Section '" + dto.getName() + "' already exists.");
        }
        section.setName(dto.getName());
        section.setStartDate(dto.getStartDate());
        section.setEndDate(dto.getEndDate());
        if (dto.getRubricId() != null) {
            Rubric rubric = rubricRepository.findById(dto.getRubricId())
                    .orElseThrow(() -> new RuntimeException("Rubric not found"));
            section.setRubric(rubric);
        }
        return sectionRepository.save(section);
    }

    public Section getById(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Section not found: " + id));
    }

    public List<Section> searchSections(String name) {
        return sectionRepository.searchByName(name);
    }

    private void generateActiveWeeks(Section section) {
        LocalDate current = section.getStartDate()
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        LocalDate end = section.getEndDate();
        List<ActiveWeek> weeks = new ArrayList<>();
        while (!current.isAfter(end)) {
            LocalDate sunday = current.plusDays(6);
            ActiveWeek week = ActiveWeek.builder()
                    .startDate(current)
                    .endDate(sunday.isAfter(end) ? end : sunday)
                    .active(true)
                    .section(section)
                    .build();
            weeks.add(week);
            current = current.plusWeeks(1);
        }
        activeWeekRepository.saveAll(weeks);
    }

    public List<ActiveWeek> getActiveWeeks(Long sectionId) {
        return activeWeekRepository.findBySectionIdOrderByStartDate(sectionId);
    }

    public void updateActiveWeeks(Long sectionId, ActiveWeekDto.BulkUpdateRequest request) {
        request.getWeeks().forEach(wu -> {
            ActiveWeek week = activeWeekRepository.findById(wu.getId())
                    .orElseThrow(() -> new RuntimeException("Week not found: " + wu.getId()));
            week.setActive(wu.isActive());
            activeWeekRepository.save(week);
        });
    }
}

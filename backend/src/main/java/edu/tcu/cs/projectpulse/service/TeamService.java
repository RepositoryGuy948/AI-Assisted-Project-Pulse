package edu.tcu.cs.projectpulse.service;

import edu.tcu.cs.projectpulse.dto.TeamDto;
import edu.tcu.cs.projectpulse.model.Section;
import edu.tcu.cs.projectpulse.model.Team;
import edu.tcu.cs.projectpulse.model.User;
import edu.tcu.cs.projectpulse.repository.SectionRepository;
import edu.tcu.cs.projectpulse.repository.TeamRepository;
import edu.tcu.cs.projectpulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;

    public Team createTeam(TeamDto dto) {
        Section section = sectionRepository.findById(dto.getSectionId())
                .orElseThrow(() -> new RuntimeException("Section not found"));
        if (teamRepository.existsByNameAndSectionId(dto.getName(), dto.getSectionId())) {
            throw new IllegalArgumentException("Team '" + dto.getName() + "' already exists in this section.");
        }
        Team team = Team.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .websiteUrl(dto.getWebsiteUrl())
                .section(section)
                .build();
        return teamRepository.save(team);
    }

    public Team updateTeam(Long id, TeamDto dto) {
        Team team = getById(id);
        if (!team.getName().equals(dto.getName()) &&
            teamRepository.existsByNameAndSectionId(dto.getName(), team.getSection().getId())) {
            throw new IllegalArgumentException("Team '" + dto.getName() + "' already exists in this section.");
        }
        team.setName(dto.getName());
        team.setDescription(dto.getDescription());
        team.setWebsiteUrl(dto.getWebsiteUrl());
        return teamRepository.save(team);
    }

    public Team getById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found: " + id));
    }

    public List<Team> searchTeams(Long sectionId, String sectionName, String teamName, String instructor) {
        return teamRepository.searchTeams(sectionId, sectionName, teamName, instructor);
    }

    public void deleteTeam(Long id) {
        Team team = getById(id);
        // Remove students from team
        team.getStudents().forEach(s -> s.setTeam(null));
        userRepository.saveAll(team.getStudents());
        teamRepository.delete(team);
    }

    public void assignStudentsToTeam(Long teamId, List<Long> studentIds) {
        Team team = getById(teamId);
        studentIds.forEach(sid -> {
            User student = userRepository.findById(sid)
                    .orElseThrow(() -> new RuntimeException("Student not found: " + sid));
            student.setTeam(team);
            userRepository.save(student);
        });
    }

    public void removeStudentFromTeam(Long teamId, Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        student.setTeam(null);
        userRepository.save(student);
    }

    public void assignInstructorsToTeam(Long teamId, List<Long> instructorIds) {
        Team team = getById(teamId);
        instructorIds.forEach(iid -> {
            User instructor = userRepository.findById(iid)
                    .orElseThrow(() -> new RuntimeException("Instructor not found: " + iid));
            team.getInstructors().add(instructor);
        });
        teamRepository.save(team);
    }

    public void removeInstructorFromTeam(Long teamId, Long instructorId) {
        Team team = getById(teamId);
        team.getInstructors().removeIf(i -> i.getId().equals(instructorId));
        teamRepository.save(team);
    }
}

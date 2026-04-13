package edu.tcu.cs.projectpulse.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ActiveWeekDto {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private Long sectionId;

    @Data
    public static class BulkUpdateRequest {
        private List<WeekUpdate> weeks;

        @Data
        public static class WeekUpdate {
            private Long id;
            private boolean active;
        }
    }
}

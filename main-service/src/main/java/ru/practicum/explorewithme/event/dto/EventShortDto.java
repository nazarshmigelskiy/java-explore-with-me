package ru.practicum.explorewithme.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.category.dto.CategoryDto;
import ru.practicum.explorewithme.dto.StatsConstants;
import ru.practicum.explorewithme.user.UserShortDto;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EventShortDto {
    private Long id;
    private String annotation;
    private String title;
    private CategoryDto category;
    private UserShortDto initiator;
    @JsonFormat(pattern = StatsConstants.TIME_FORMAT_PATTERN)
    private LocalDateTime eventDate;
    private Boolean paid;
    private Long confirmedRequests;
    private Long views;
}

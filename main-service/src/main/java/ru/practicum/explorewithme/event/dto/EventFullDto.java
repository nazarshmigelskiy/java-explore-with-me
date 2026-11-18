package ru.practicum.explorewithme.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.category.dto.CategoryDto;
import ru.practicum.explorewithme.dto.StatsConstants;
import ru.practicum.explorewithme.event.Location;
import ru.practicum.explorewithme.event.enums.EventState;
import ru.practicum.explorewithme.user.UserShortDto;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EventFullDto {
    private Long id;
    private String annotation;
    private String description;
    private String title;
    private CategoryDto category;
    private UserShortDto initiator;
    private Location location;
    @JsonFormat(pattern = StatsConstants.TIME_FORMAT_PATTERN)
    private LocalDateTime createdOn;
    @JsonFormat(pattern = StatsConstants.TIME_FORMAT_PATTERN)
    private LocalDateTime eventDate;
    @JsonFormat(pattern = StatsConstants.TIME_FORMAT_PATTERN)
    private LocalDateTime publishedOn;
    private Boolean paid;
    private Boolean requestModeration;
    private Integer participantLimit;
    private EventState state;
    private Long confirmedRequests;
    private Long views;
    private Long commentsCount;
}

package ru.practicum.explorewithme.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.dto.StatsConstants;
import ru.practicum.explorewithme.event.Location;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {
    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;
    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;
    @NotBlank
    @Size(min = 3, max = 120)
    private String title;
    @NotNull
    private Long category;
    @NotNull
    private Location location;
    @NotNull
    @JsonFormat(pattern = StatsConstants.TIME_FORMAT_PATTERN)
    private LocalDateTime eventDate;
    private Boolean paid = false;
    private Boolean requestModeration = true;
    @PositiveOrZero
    private Integer participantLimit = 0;
}

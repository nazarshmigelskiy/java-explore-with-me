package ru.practicum.explorewithme.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.explorewithme.dto.StatsConstants;
import ru.practicum.explorewithme.request.RequestStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {
    private Long id;
    @JsonFormat(pattern = StatsConstants.TIME_FORMAT_PATTERN)
    private LocalDateTime created;
    private Long event;
    private Long requester;
    private RequestStatus status;
}

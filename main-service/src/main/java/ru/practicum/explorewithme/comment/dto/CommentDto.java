package ru.practicum.explorewithme.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.explorewithme.dto.StatsConstants;
import ru.practicum.explorewithme.user.UserShortDto;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    private Long id;
    private String text;
    private Long eventId;
    private UserShortDto author;
    @JsonFormat(pattern = StatsConstants.TIME_FORMAT_PATTERN)
    private LocalDateTime createdOn;
    @JsonFormat(pattern = StatsConstants.TIME_FORMAT_PATTERN)
    private LocalDateTime updatedOn;

}

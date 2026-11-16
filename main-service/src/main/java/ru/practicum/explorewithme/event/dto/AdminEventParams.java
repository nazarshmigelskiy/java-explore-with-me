package ru.practicum.explorewithme.event.dto;

import ru.practicum.explorewithme.event.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;

public record AdminEventParams(List<Long> users,
                               List<EventState> states,
                               List<Long> categories,
                               LocalDateTime rangeStart,
                               LocalDateTime rangeEnd,
                               int from,
                               int size) {
}

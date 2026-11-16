package ru.practicum.explorewithme.event.dto;

import ru.practicum.explorewithme.event.enums.EventSort;

import java.time.LocalDateTime;
import java.util.List;

public record PublicEventParams(String text,
                                List<Long> categories,
                                Boolean paid,
                                LocalDateTime rangeStart,
                                LocalDateTime rangeEnd,
                                boolean onlyAvailable,
                                EventSort sort,
                                int from,
                                int size) {
}

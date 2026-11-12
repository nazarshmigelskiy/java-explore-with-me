package ru.practicum.explorewithme.event;

import lombok.experimental.UtilityClass;
import ru.practicum.explorewithme.category.CategoryMapper;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.dto.NewEventDto;
import ru.practicum.explorewithme.user.UserMapper;

import java.time.LocalDateTime;

@UtilityClass
public class EventMapper {
    public static Event toEventFromNewEventDto(NewEventDto newEventDto) {
        return new Event(
                null,
                newEventDto.getAnnotation(),
                newEventDto.getDescription(),
                newEventDto.getTitle(),
                null,
                null,
                newEventDto.getLocation(),
                LocalDateTime.now(),
                newEventDto.getEventDate(),
                null,
                newEventDto.getPaid(),
                newEventDto.getRequestModeration(),
                newEventDto.getParticipantLimit(),
                EventState.PENDING
        );
    }

    public static EventShortDto toShortDto(Event event, long confirmedRequests, long views) {
        return new EventShortDto(
                event.getId(),
                event.getAnnotation(),
                event.getTitle(),
                CategoryMapper.toCategoryDto(event.getCategory()),
                UserMapper.toUserShortDto(event.getInitiator()),
                event.getEventDate(),
                event.getPaid(),
                confirmedRequests,
                views
        );
    }

    public static EventFullDto toFullDto(Event event, long confirmedRequests, long views) {
        return new EventFullDto(
                event.getId(),
                event.getAnnotation(),
                event.getDescription(),
                event.getTitle(),
                CategoryMapper.toCategoryDto(event.getCategory()),
                UserMapper.toUserShortDto(event.getInitiator()),
                event.getLocation(),
                event.getCreatedOn(),
                event.getEventDate(),
                event.getPublishedOn(),
                event.getPaid(),
                event.getRequestModeration(),
                event.getParticipantLimit(),
                event.getState(),
                confirmedRequests,
                views
        );
    }

}

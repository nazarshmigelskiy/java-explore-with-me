package ru.practicum.explorewithme.event;

import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.dto.NewEventDto;
import ru.practicum.explorewithme.event.dto.UpdateEventUserRequest;

import java.util.List;

public interface EventService {
    EventFullDto create(NewEventDto newEventDto, long userId);

    List<EventShortDto> getEventsByUserId(long userId, int from, int size);

    EventFullDto getEventByUserIdAndEventId(long userId, long eventId);

    EventFullDto update(UpdateEventUserRequest updateEventUserRequest, long userId, long eventId);

}

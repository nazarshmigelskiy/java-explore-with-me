package ru.practicum.explorewithme.event;

import ru.practicum.explorewithme.event.dto.*;

import java.util.List;

public interface EventService {
    EventFullDto create(NewEventDto newEventDto, long userId);

    List<EventShortDto> getEventsByUserId(long userId, int from, int size);

    EventFullDto getEventByUserIdAndEventId(long userId, long eventId);

    EventFullDto update(UpdateEventUserRequest updateEventUserRequest, long userId, long eventId);

    List<EventFullDto> getEventsByAdmin(AdminEventParams params);

    EventFullDto updateByAdmin(long eventId, UpdateEventAdminRequest request);

    EventFullDto getPublishedEvent(long eventId, String ip, String uri);

    List<EventShortDto> getPublishedEvents(PublicEventParams params, String ip, String uri);

}

package ru.practicum.explorewithme.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.category.Category;
import ru.practicum.explorewithme.category.CategoryRepository;
import ru.practicum.explorewithme.event.dto.*;
import ru.practicum.explorewithme.exception.BadRequestException;
import ru.practicum.explorewithme.exception.ConflictException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.user.User;
import ru.practicum.explorewithme.user.UserRepository;
import ru.practicum.explorewithme.util.OffsetPageRequest;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public EventFullDto create(NewEventDto newEventDto, long userId) {
        checkEventDate(newEventDto.getEventDate());
        Event event = EventMapper.toEventFromNewEventDto(newEventDto);
        event.setCategory(getCategory(newEventDto.getCategory()));
        event.setInitiator(getUser(userId));
        return EventMapper.toFullDto(eventRepository.save(event), 0L, 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByUserId(long userId, int from, int size) {
        OffsetPageRequest request = new OffsetPageRequest(from, size, Sort.by("id"));
        return eventRepository.findAllByInitiatorId(userId, request).stream()
                .map(event -> EventMapper.toShortDto(event, 0L, 0L))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByUserIdAndEventId(long userId, long eventId) {
        return EventMapper.toFullDto(eventRepository.findByIdAndInitiatorId(eventId, userId)
                        .orElseThrow(() -> new NotFoundException("Событие с таким id не найдено")),
                0L, 0L);
    }

    @Override
    @Transactional
    public EventFullDto update(UpdateEventUserRequest request, long userId, long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с таким id не найдено"));

        if (event.getState() == EventState.PUBLISHED) throw new ConflictException("Данное событие уже опубликовано");

        if (request.getStateAction() != null) {
            event.setState(
                    switch (request.getStateAction()) {
                        case SEND_TO_REVIEW -> EventState.PENDING;
                        case CANCEL_REVIEW -> EventState.CANCELED;
                    });
        }

        applyUpdate(event, request);

        return EventMapper.toFullDto(eventRepository.save(event), 0L, 0L);
    }

    private void applyUpdate(Event event, UpdateEventRequest request) {
        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());

        if (request.getDescription() != null)
            event.setDescription(request.getDescription());

        if (request.getTitle() != null)
            event.setTitle(request.getTitle());

        if (request.getCategory() != null)
            event.setCategory(getCategory(request.getCategory()));

        if (request.getLocation() != null)
            event.setLocation(request.getLocation());

        if (request.getEventDate() != null) {
            checkEventDate(request.getEventDate());
            event.setEventDate(request.getEventDate());
        }

        if (request.getPaid() != null)
            event.setPaid(request.getPaid());

        if (request.getRequestModeration() != null)
            event.setRequestModeration(request.getRequestModeration());

        if (request.getParticipantLimit() != null)
            event.setParticipantLimit(request.getParticipantLimit());

    }

    private void checkEventDate(LocalDateTime time) {
        if (time.isBefore(LocalDateTime.now().plusHours(2)))
            throw new BadRequestException("Дата начала события раньше чем через 2 часа от текущего момента");
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с таким id не найден"));
    }

    private Category getCategory(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с таким id не найдена"));
    }
}

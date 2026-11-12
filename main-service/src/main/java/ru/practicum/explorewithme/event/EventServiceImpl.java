package ru.practicum.explorewithme.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.category.Category;
import ru.practicum.explorewithme.category.CategoryRepository;
import ru.practicum.explorewithme.event.dto.*;
import ru.practicum.explorewithme.event.enums.AdminStateAction;
import ru.practicum.explorewithme.event.enums.EventState;
import ru.practicum.explorewithme.exception.BadRequestException;
import ru.practicum.explorewithme.exception.ConflictException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.user.User;
import ru.practicum.explorewithme.user.UserRepository;
import ru.practicum.explorewithme.util.OffsetPageRequest;
import static ru.practicum.explorewithme.event.EventSpecification.*;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private static final int MIN_HOURS_FOR_USER = 2;
    private static final int MIN_HOURS_FOR_ADMIN = 1;

    @Override
    @Transactional
    public EventFullDto create(NewEventDto newEventDto, long userId) {
        checkEventDate(newEventDto.getEventDate(), MIN_HOURS_FOR_USER);
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

        applyUpdate(event, request, MIN_HOURS_FOR_USER);

        return EventMapper.toFullDto(event, 0L, 0L);
    }

    @Override
    @Transactional
    public EventFullDto updateByAdmin(long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с таким id не найдено"));
        AdminStateAction action = request.getStateAction();

        if (action == AdminStateAction.PUBLISH_EVENT && event.getState() != EventState.PENDING)
            throw new ConflictException("Опубликовать можно только событие, ожидающее модерации. Текущее состояние: "
                    + event.getState());

        if (action == AdminStateAction.REJECT_EVENT && event.getState() == EventState.PUBLISHED)
            throw new ConflictException("Нельзя отклонить опубликованное событие");

        applyUpdate(event, request, MIN_HOURS_FOR_ADMIN);

        if (action == AdminStateAction.PUBLISH_EVENT) {
            if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(MIN_HOURS_FOR_ADMIN))) {
                throw new ConflictException("До начала события должно оставаться не меньше часа");
            }
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        } else if (action == AdminStateAction.REJECT_EVENT) {
            event.setState(EventState.CANCELED);
        }
        return EventMapper.toFullDto(event, 0L, 0L);
    }


    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEventsByAdmin(AdminEventParams params) {
        if (params.rangeStart() != null && params.rangeEnd() != null
                && params.rangeStart().isAfter(params.rangeEnd()))
            throw new BadRequestException("Начало диапазона дат позже его конца");

        Specification<Event> spec = Specification.where(initiatorIn(params.users()))
                .and(stateIn(params.states()))
                .and(categoryIn(params.categories()))
                .and(eventDateFrom(params.rangeStart()))
                .and(eventDateTo(params.rangeEnd()));

        OffsetPageRequest page = new OffsetPageRequest(params.from(), params.size(), Sort.by("id"));

        return eventRepository.findAll(spec, page).stream()
                .map(event -> EventMapper.toFullDto(event, 0L, 0L))
                .toList();
    }

    private void applyUpdate(Event event, UpdateEventRequest request, int minHour) {
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
            checkEventDate(request.getEventDate(), minHour);
            event.setEventDate(request.getEventDate());
        }

        if (request.getPaid() != null)
            event.setPaid(request.getPaid());

        if (request.getRequestModeration() != null)
            event.setRequestModeration(request.getRequestModeration());

        if (request.getParticipantLimit() != null)
            event.setParticipantLimit(request.getParticipantLimit());

    }

    private void checkEventDate(LocalDateTime time, int minHours) {
        if (time.isBefore(LocalDateTime.now().plusHours(minHours)))
            throw new BadRequestException("Дата начала события раньше чем через "
                    + minHours + " ч. от текущего момента");
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

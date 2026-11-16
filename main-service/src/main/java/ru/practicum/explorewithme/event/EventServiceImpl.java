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
import ru.practicum.explorewithme.event.enums.EventSort;
import ru.practicum.explorewithme.event.enums.EventState;
import ru.practicum.explorewithme.exception.BadRequestException;
import ru.practicum.explorewithme.exception.ConflictException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.user.User;
import ru.practicum.explorewithme.user.UserRepository;
import ru.practicum.explorewithme.util.OffsetPageRequest;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static ru.practicum.explorewithme.event.EventSpecification.*;


@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventStatService statService;
    private static final int MIN_HOURS_FOR_USER = 2;
    private static final int MIN_HOURS_FOR_ADMIN = 1;

    @Override
    @Transactional
    public EventFullDto create(NewEventDto newEventDto, long userId) {
        checkEventDate(newEventDto.getEventDate(), MIN_HOURS_FOR_USER);
        Event event = EventMapper.toEventFromNewEventDto(newEventDto);
        event.setCategory(getCategory(newEventDto.getCategory()));
        event.setInitiator(getUser(userId));
        return toFullDto(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByUserId(long userId, int from, int size) {
        OffsetPageRequest request = new OffsetPageRequest(from, size, Sort.by("id"));
        List<Event> events = eventRepository.findAllByInitiatorId(userId, request);
        return toShortDtos(events);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByUserIdAndEventId(long userId, long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с таким id не найдено"));
        return toFullDto(event);
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


        return toFullDto(event);
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

        return toFullDto(event);
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

        List<Event> events = eventRepository.findAll(spec, page).toList();

        return toFullDtos(events);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getPublishedEvent(long eventId, String ip, String uri) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
        statService.saveHit(uri, ip);
        return toFullDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getPublishedEvents(PublicEventParams params, String ip, String uri) {
        if (params.rangeStart() != null && params.rangeEnd() != null
                && params.rangeStart().isAfter(params.rangeEnd())) {
            throw new BadRequestException("Начало диапазона дат позже его конца");
        }

        LocalDateTime start = params.rangeStart();
        if (params.rangeStart() == null && params.rangeEnd() == null) {
            start = LocalDateTime.now();
        }

        Specification<Event> spec = Specification.where(stateIn(List.of(EventState.PUBLISHED)))
                .and(textContains(params.text()))
                .and(categoryIn(params.categories()))
                .and(paidEquals(params.paid()))
                .and(eventDateFrom(start))
                .and(eventDateTo(params.rangeEnd()))
                .and(onlyAvailable(params.onlyAvailable()));

        statService.saveHit(uri, ip);
        if (params.sort() == EventSort.VIEWS) {
            List<Event> all = eventRepository.findAll(spec);
            return toShortDtos(all).stream()
                    .sorted(Comparator.comparing(EventShortDto::getViews).reversed())
                    .skip(params.from())
                    .limit(params.size())
                    .toList();
        }

        Sort sort = params.sort() == EventSort.EVENT_DATE ? Sort.by("eventDate") : Sort.by("id");
        List<Event> events = eventRepository
                .findAll(spec, new OffsetPageRequest(params.from(), params.size(), sort))
                .getContent();

        return toShortDtos(events);
    }

    private List<EventFullDto> toFullDtos(List<Event> events) {
        Map<Long, Long> confirmed = statService.getConfirmedCounts(events);
        Map<Long, Long> views = statService.getViews(events);

        return events.stream()
                .map(e -> EventMapper.toFullDto(e,
                        confirmed.getOrDefault(e.getId(), 0L),
                        views.getOrDefault(e.getId(), 0L)))
                .toList();
    }

    private EventFullDto toFullDto(Event event) {
        return toFullDtos(List.of(event)).getFirst();
    }

    private List<EventShortDto> toShortDtos(List<Event> events) {
        Map<Long, Long> confirmed = statService.getConfirmedCounts(events);
        Map<Long, Long> views = statService.getViews(events);

        return events.stream()
                .map(e -> EventMapper.toShortDto(e,
                        confirmed.getOrDefault(e.getId(), 0L),
                        views.getOrDefault(e.getId(), 0L)))
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

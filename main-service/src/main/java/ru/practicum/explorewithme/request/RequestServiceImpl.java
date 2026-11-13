package ru.practicum.explorewithme.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.event.Event;
import ru.practicum.explorewithme.event.EventRepository;
import ru.practicum.explorewithme.event.enums.EventState;
import ru.practicum.explorewithme.exception.BadRequestException;
import ru.practicum.explorewithme.exception.ConflictException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.request.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.user.User;
import ru.practicum.explorewithme.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ParticipationRequestDto create(long userId, long eventId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (event.getInitiator().getId().equals(userId))
            throw new ConflictException("Нельзя подавать заявку на собственное событие");
        if (event.getState() != EventState.PUBLISHED)
            throw new ConflictException("Нельзя участвовать в неопубликованном собитии");
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId))
            throw new ConflictException("Заявка на событие уже подана");
        if (event.getParticipantLimit() > 0
                && requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)
                >= event.getParticipantLimit()) throw new ConflictException("Достигнут лимит участников события");

        ParticipationRequest request = new ParticipationRequest();
        request.setEvent(event);
        request.setRequester(user);

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        request.setCreated(LocalDateTime.now());

        return RequestMapper.toDto(requestRepository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(long userId) {
        if (!userRepository.existsById(userId)) throw new NotFoundException("Пользователь не найден");

        return requestRepository.findAllByRequesterId(userId).stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(long userId, long requestId) {
        ParticipationRequest request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Заявка не найдена"));

        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.toDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsForEvent(long userId, long eventId) {
        eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        return requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateState(EventRequestStatusUpdateRequest updateRequest,
                                                      long userId, long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (updateRequest.getStatus() != RequestStatus.CONFIRMED && updateRequest.getStatus() != RequestStatus.REJECTED)
            throw new BadRequestException("Недопустимый статус обновления");
        List<ParticipationRequest> rejectedList = new ArrayList<>();
        List<ParticipationRequest> confirmedList = new ArrayList<>();

        List<ParticipationRequest> requests = requestRepository.findAllById(updateRequest.getRequestIds());

        for (ParticipationRequest request : requests) {
            if (!request.getEvent().getId().equals(eventId)) throw new ConflictException("Заявка с id "
                    + request.getId() + " не относится к этому событию");
            if (request.getStatus() != RequestStatus.PENDING) throw new ConflictException("Заявка с id "
                    + request.getId() + " не находится в списке для подтверждения");
        }
        int limit = event.getParticipantLimit();
        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        switch (updateRequest.getStatus()) {
            case REJECTED -> {
                for (ParticipationRequest request : requests) {
                    request.setStatus(RequestStatus.REJECTED);
                    rejectedList.add(request);
                }
            }
            case CONFIRMED -> {
                if (limit != 0 && confirmed >= limit) throw new ConflictException("Достигнут лимит участников");
                for (ParticipationRequest request : requests) {
                    if (limit == 0 || confirmed < limit) {
                        request.setStatus(RequestStatus.CONFIRMED);
                        confirmed++;
                        confirmedList.add(request);
                    } else {
                        request.setStatus(RequestStatus.REJECTED);
                        rejectedList.add(request);
                    }
                }
            }
        }
        if (limit != 0 && confirmed >= limit) {
            List<ParticipationRequest> leftRequests = requestRepository
                    .findAllByEventIdAndStatus(eventId, RequestStatus.PENDING);
            for (ParticipationRequest leftRequest : leftRequests) {
                leftRequest.setStatus(RequestStatus.REJECTED);
            }
            rejectedList.addAll(leftRequests);
        }

        return new EventRequestStatusUpdateResult(
                confirmedList.stream()
                        .map(RequestMapper::toDto)
                        .toList(),
                rejectedList.stream()
                        .map(RequestMapper::toDto)
                        .toList()
        );
    }


}

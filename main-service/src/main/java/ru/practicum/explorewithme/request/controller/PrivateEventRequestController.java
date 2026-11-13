package ru.practicum.explorewithme.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.request.RequestService;
import ru.practicum.explorewithme.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.request.dto.ParticipationRequestDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events/{eventId}/requests")
public class PrivateEventRequestController {
    private final RequestService service;

    @GetMapping
    public List<ParticipationRequestDto> getRequestsForEvent(@PathVariable Long userId, @PathVariable Long eventId) {
        return service.getRequestsForEvent(userId, eventId);
    }

    @PatchMapping
    public EventRequestStatusUpdateResult updateState(@Valid @RequestBody EventRequestStatusUpdateRequest request,
                                                      @PathVariable Long userId, @PathVariable Long eventId) {
        return service.updateState(request, userId, eventId);
    }
}

package ru.practicum.explorewithme.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.EndpointHitDto;
import ru.practicum.explorewithme.dto.StatsConstants;
import ru.practicum.explorewithme.dto.ViewStatsDto;
import ru.practicum.explorewithme.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService service;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void createHit(@Valid @RequestBody EndpointHitDto dto) {
        service.createHit(dto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@RequestParam
                                       @DateTimeFormat(pattern = StatsConstants.TIME_FORMAT_PATTERN)
                                       LocalDateTime start,
                                       @RequestParam
                                       @DateTimeFormat(pattern = StatsConstants.TIME_FORMAT_PATTERN)
                                       LocalDateTime end,
                                       @RequestParam(required = false) List<String> uris,
                                       @RequestParam(required = false) boolean unique) {
        return service.getStats(start, end, unique, uris);
    }

}

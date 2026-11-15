package ru.practicum.explorewithme.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.client.StatsClient;
import ru.practicum.explorewithme.dto.EndpointHitDto;
import ru.practicum.explorewithme.dto.ViewStatsDto;
import ru.practicum.explorewithme.request.EventRequestCount;
import ru.practicum.explorewithme.request.RequestRepository;
import ru.practicum.explorewithme.request.RequestStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventStatService {
    private static final String EVENT_URI_PREFIX = "/events/";
    private static final String APP_NAME = "ewm-main-service";

    private final RequestRepository requestRepository;
    private final StatsClient statsClient;

    public void saveHit(String uri, String ip) {
        statsClient.sendHit(new EndpointHitDto(APP_NAME, uri, ip, LocalDateTime.now()));
    }

    public Map<Long, Long> getConfirmedCounts(Collection<Event> events) {
        List<Long> ids = events.stream().map(Event::getId).toList();
        return requestRepository.countByEventIds(ids, RequestStatus.CONFIRMED).stream()
                .collect(Collectors.toMap(EventRequestCount::getEventId, EventRequestCount::getCount));
    }

    public Map<Long, Long> getViews(Collection<Event> events) {
        List<Event> published = events.stream()
                .filter(e -> e.getPublishedOn() != null)
                .toList();
        if (published.isEmpty()) {
            return Map.of();
        }

        LocalDateTime start = published.stream()
                .map(Event::getPublishedOn)
                .min(LocalDateTime::compareTo)
                .get();
        List<String> uris = published.stream()
                .map(e -> EVENT_URI_PREFIX + e.getId())
                .toList();

        return statsClient.getStats(start, LocalDateTime.now(), uris, true).stream()
                .collect(Collectors.toMap(
                        s -> Long.parseLong(s.getUri().substring(EVENT_URI_PREFIX.length())),
                        ViewStatsDto::getHits));
    }
}

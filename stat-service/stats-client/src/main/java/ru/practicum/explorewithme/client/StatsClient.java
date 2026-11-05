package ru.practicum.explorewithme.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.explorewithme.dto.EndpointHitDto;
import ru.practicum.explorewithme.dto.StatsConstants;
import ru.practicum.explorewithme.dto.ViewStatsDto;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class StatsClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public StatsClient(RestTemplateBuilder builder,
                       @Value("${stats-server.url}") String baseUrl) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
        this.baseUrl = baseUrl;
    }

    public void sendHit(EndpointHitDto dto) {
        URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/hit")
                .build()
                .toUri();

        try {
            restTemplate.postForEntity(uri, dto, Void.class);
        } catch (RestClientException e) {
            log.warn("Не удалось отправить запрос: {}", e.getMessage());
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(StatsConstants.TIME_FORMAT_PATTERN);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/stats")
                .queryParam("start", start.format(formatter))
                .queryParam("end", end.format(formatter))
                .queryParam("unique", unique);
        if (uris != null && !uris.isEmpty()) {
            builder.queryParam("uris", uris);
        }
        URI uri = builder.encode().build().toUri();
        try {
            ViewStatsDto[] stats = restTemplate.getForObject(uri, ViewStatsDto[].class);
            return stats == null ? List.of() : List.of(stats);
        } catch (RestClientException e) {
            log.warn("Не удалось получить данные статистики: {}", e.getMessage());
            return List.of();
        }
    }
}

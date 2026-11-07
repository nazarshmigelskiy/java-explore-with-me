package ru.practicum.explorewithme.service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.mapper.EndpointHitMapper;
import ru.practicum.explorewithme.dto.EndpointHitDto;
import ru.practicum.explorewithme.dto.ViewStatsDto;
import ru.practicum.explorewithme.exception.BadRequestException;
import ru.practicum.explorewithme.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final EndpointHitRepository repository;

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       boolean unique,
                                       List<String> uris) {
        if (start.isAfter(end)) {
            throw new BadRequestException("Неверный диапазон выгрузки статистики");
        }
        if (uris == null || uris.isEmpty()) {
            if (unique) {
                return repository.findAllUniqueStats(start, end);
            } else {
                return repository.findAllStats(start, end);
            }
        }
        if (unique) {
            return repository.findUniqueStatsByUris(start, end, uris);
        } else {
            return repository.findStatsByUris(start, end, uris);
        }
    }

    @Override
    @Transactional
    public void createHit(EndpointHitDto endpointHitDto) {
        repository.save(EndpointHitMapper.toEndpointHit(endpointHitDto));
    }
}

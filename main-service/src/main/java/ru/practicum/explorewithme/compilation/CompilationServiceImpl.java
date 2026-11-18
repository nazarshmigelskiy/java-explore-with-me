package ru.practicum.explorewithme.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.compilation.dto.CompilationDto;
import ru.practicum.explorewithme.compilation.dto.NewCompilationDto;
import ru.practicum.explorewithme.compilation.dto.UpdateCompilationRequest;
import ru.practicum.explorewithme.event.Event;
import ru.practicum.explorewithme.event.EventMapper;
import ru.practicum.explorewithme.event.EventRepository;
import ru.practicum.explorewithme.event.EventStatService;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.util.OffsetPageRequest;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final EventStatService statService;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto dto) {
        Set<Event> events = new HashSet<>(eventRepository.findAllById(dto.getEvents()));
        if (events.size() != dto.getEvents().size())
            throw new NotFoundException("В подборке указаны несуществующие события");
        Compilation compilation = compilationRepository.save(
                new Compilation(null, dto.getTitle(), dto.getPinned(), events));
        return toDto(compilation);
    }

    @Override
    @Transactional
    public CompilationDto update(long compilationId, UpdateCompilationRequest request) {
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Подборка не найдена"));

        if (request.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(request.getEvents()));
            if (events.size() != request.getEvents().size())
                throw new NotFoundException("В подборке указаны несуществующие события");
            compilation.setEvents(events);
        }
        if (request.getPinned() != null) compilation.setPinned(request.getPinned());
        if (request.getTitle() != null) compilation.setTitle(request.getTitle());

        return toDto(compilation);
    }

    @Override
    @Transactional
    public void delete(long compilationId) {
        if (!compilationRepository.existsById(compilationId)) throw new NotFoundException("Подборка не найдена");
        compilationRepository.deleteById(compilationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        OffsetPageRequest page = new OffsetPageRequest(from, size, Sort.by("id"));
        List<Compilation> compilations = pinned == null
                ? compilationRepository.findAll(page).getContent()
                : compilationRepository.findAllByPinned(pinned, page);

        return toDtos(compilations);
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilation(long compilationId) {
        return toDto(compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Подборка не найдена")));
    }


    private List<CompilationDto> toDtos(List<Compilation> compilations) {
        Set<Event> allEvents = compilations.stream()
                .flatMap(c -> c.getEvents().stream())
                .collect(Collectors.toSet());
        Map<Long, Long> confirmed = statService.getConfirmedCounts(allEvents);
        Map<Long, Long> views = statService.getViews(allEvents);
        Map<Long, Long> comments = statService.getCommentCounts(allEvents);

        return compilations.stream()
                .map(c -> CompilationMapper.toDto(c, c.getEvents().stream()
                        .map(e -> EventMapper.toShortDto(e, confirmed.getOrDefault(e.getId(), 0L),
                                views.getOrDefault(e.getId(), 0L),
                                comments.getOrDefault(e.getId(), 0L)))
                        .toList()))
                .toList();
    }

    private CompilationDto toDto(Compilation compilation) {
        return toDtos(List.of(compilation)).getFirst();
    }


}

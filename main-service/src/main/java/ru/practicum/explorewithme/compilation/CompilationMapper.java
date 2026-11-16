package ru.practicum.explorewithme.compilation;

import lombok.experimental.UtilityClass;
import ru.practicum.explorewithme.compilation.dto.CompilationDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;

import java.util.List;

@UtilityClass
public class CompilationMapper {
    public static CompilationDto toDto(Compilation compilation, List<EventShortDto> events) {
        return new CompilationDto(
                compilation.getId(),
                compilation.getTitle(),
                compilation.getPinned(),
                events
        );
    }
}

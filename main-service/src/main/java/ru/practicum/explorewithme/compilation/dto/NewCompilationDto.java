package ru.practicum.explorewithme.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {
    private Set<Long> events = new HashSet<>();
    @NotBlank
    @Size(max = 50)
    private String title;
    private Boolean pinned = false;
}

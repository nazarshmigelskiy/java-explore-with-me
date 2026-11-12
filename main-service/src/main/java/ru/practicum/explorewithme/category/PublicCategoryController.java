package ru.practicum.explorewithme.category;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class PublicCategoryController {
    private final CategoryService categoryService;

    @GetMapping
    List<CategoryDto> getCategories(@RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                    @RequestParam(defaultValue = "10") @Positive int size) {
        return categoryService.getCategories(from, size);
    }

    @GetMapping("/{catId}")
    CategoryDto getCategory(@PathVariable Long catId) {
        return categoryService.getCategory(catId);
    }

}

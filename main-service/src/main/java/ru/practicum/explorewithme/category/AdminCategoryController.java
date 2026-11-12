package ru.practicum.explorewithme.category;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class AdminCategoryController {
    private final CategoryService categoryService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CategoryDto create(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        return categoryService.create(newCategoryDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{catId}")
    public CategoryDto update(@Valid @RequestBody NewCategoryDto newCategoryDto, @PathVariable long catId) {
        return categoryService.update(newCategoryDto, catId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{catId}")
    public void delete(@PathVariable long catId) {
        categoryService.delete(catId);
    }


}

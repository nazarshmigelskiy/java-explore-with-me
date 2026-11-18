package ru.practicum.explorewithme.category;

import lombok.experimental.UtilityClass;
import ru.practicum.explorewithme.category.dto.CategoryDto;
import ru.practicum.explorewithme.category.dto.NewCategoryDto;

@UtilityClass
public class CategoryMapper {
    public static Category toCategory(NewCategoryDto newCategoryDto) {
        return new Category(
                null,
                newCategoryDto.getName()
        );
    }

    public static CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName()
        );
    }
}

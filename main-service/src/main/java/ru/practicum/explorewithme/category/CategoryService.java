package ru.practicum.explorewithme.category;

import java.util.List;

public interface CategoryService {

    CategoryDto create(NewCategoryDto newCategoryDto);

    CategoryDto update(NewCategoryDto newCategoryDto, long id);

    void delete(long id);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategory(long id);

}

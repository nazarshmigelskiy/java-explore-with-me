package ru.practicum.explorewithme.category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.category.dto.CategoryDto;
import ru.practicum.explorewithme.category.dto.NewCategoryDto;
import ru.practicum.explorewithme.event.EventRepository;
import ru.practicum.explorewithme.exception.ConflictException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.util.OffsetPageRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto newCategoryDto) {
        return CategoryMapper.toCategoryDto(categoryRepository.save(CategoryMapper.toCategory(newCategoryDto)));
    }

    @Override
    @Transactional
    public CategoryDto update(NewCategoryDto newCategoryDto, long id) {
        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Категории с таким id не существует"));
        category.setName(newCategoryDto.getName());
        return CategoryMapper.toCategoryDto(categoryRepository.saveAndFlush(category));
    }

    @Override
    @Transactional
    public void delete(long id) {
        if (!categoryRepository.existsById(id)) throw new NotFoundException("Категории с таким id не существует");
        if (eventRepository.existsByCategoryId(id))
            throw new ConflictException("Для данной категории есть запланированные события");

        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(int from, int size) {
        OffsetPageRequest request = new OffsetPageRequest(from, size, Sort.by("id"));
        return categoryRepository.findAll(request).getContent().stream()
                .map(CategoryMapper::toCategoryDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategory(long id) {
        return CategoryMapper.toCategoryDto(categoryRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Категории с таким id не существует")));
    }
}

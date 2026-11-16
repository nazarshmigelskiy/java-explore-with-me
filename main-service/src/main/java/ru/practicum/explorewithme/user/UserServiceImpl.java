package ru.practicum.explorewithme.user;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.util.OffsetPageRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDto create(NewUserRequest newUserRequest) {
        return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(newUserRequest)));
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        OffsetPageRequest request = new OffsetPageRequest(from, size, Sort.by("id"));
        if (ids == null || ids.isEmpty()) {
            return userRepository.findAll(request).getContent().stream()
                    .map(UserMapper::toUserDto)
                    .toList();
        } else {
            return userRepository.findByIdIn(ids, request).stream()
                    .map(UserMapper::toUserDto)
                    .toList();
        }
    }

    @Transactional
    @Override
    public void delete(long userId) {
        if (!userRepository.existsById(userId)) throw new NotFoundException("Пользователя с таким id не существует");
        userRepository.deleteById(userId);
    }
}

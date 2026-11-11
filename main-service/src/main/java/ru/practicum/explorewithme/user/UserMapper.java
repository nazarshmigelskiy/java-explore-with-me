package ru.practicum.explorewithme.user;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserMapper {
    public static UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public static User toUser(NewUserRequest userRequest) {
        return new User(
                null,
                userRequest.getName(),
                userRequest.getEmail()
        );
    }
}

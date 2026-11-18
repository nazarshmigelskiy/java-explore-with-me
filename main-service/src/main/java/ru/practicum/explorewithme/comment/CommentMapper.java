package ru.practicum.explorewithme.comment;

import lombok.experimental.UtilityClass;
import ru.practicum.explorewithme.comment.dto.CommentDto;
import ru.practicum.explorewithme.user.UserMapper;

@UtilityClass
public class CommentMapper {
    public static CommentDto toDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getEvent().getId(),
                UserMapper.toUserShortDto(comment.getAuthor()),
                comment.getCreatedOn(),
                comment.getUpdatedOn());
    }
}

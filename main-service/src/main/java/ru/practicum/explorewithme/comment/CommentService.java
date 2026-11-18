package ru.practicum.explorewithme.comment;

import ru.practicum.explorewithme.comment.dto.CommentDto;
import ru.practicum.explorewithme.comment.dto.NewCommentDto;

import java.util.List;

public interface CommentService {
    CommentDto create(NewCommentDto dto, Long userId, Long eventId);

    CommentDto update(NewCommentDto dto, Long userId, Long commentId);

    void delete(Long userId, Long commentId);

    List<CommentDto> getUserComments(Long userId, int from, int size);
}

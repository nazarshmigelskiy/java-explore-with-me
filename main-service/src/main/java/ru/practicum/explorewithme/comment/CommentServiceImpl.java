package ru.practicum.explorewithme.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.comment.dto.CommentDto;
import ru.practicum.explorewithme.comment.dto.NewCommentDto;
import ru.practicum.explorewithme.event.Event;
import ru.practicum.explorewithme.event.EventRepository;
import ru.practicum.explorewithme.event.enums.EventState;
import ru.practicum.explorewithme.exception.ConflictException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.user.User;
import ru.practicum.explorewithme.user.UserRepository;
import ru.practicum.explorewithme.util.OffsetPageRequest;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;


    @Override
    @Transactional
    public CommentDto create(NewCommentDto dto, Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь не найден"));
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие не найдено"));
        if (event.getState() != EventState.PUBLISHED)
            throw new ConflictException("Комментировать можно только опубликованные события");

        return CommentMapper.toDto(commentRepository.save(new Comment(
                null, dto.getText(), event, user, LocalDateTime.now(), null)));
    }

    @Override
    @Transactional
    public CommentDto update(NewCommentDto dto, Long userId, Long commentId) {
        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId).orElseThrow(
                () -> new NotFoundException("Комментарий не найден"));

        comment.setText(dto.getText());
        comment.setUpdatedOn(LocalDateTime.now());

        return CommentMapper.toDto(comment);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long commentId) {
        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId).orElseThrow(
                () -> new NotFoundException("Комментарий не найден"));

        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getUserComments(Long userId, int from, int size) {
        if (!userRepository.existsById(userId)) throw new NotFoundException("Пользователь не найден");

        OffsetPageRequest page = new OffsetPageRequest(from, size, Sort.by(Sort.Direction.DESC, "createdOn"));

        return commentRepository.findAllByAuthorId(userId, page).stream()
                .map(CommentMapper::toDto)
                .toList();
    }
}

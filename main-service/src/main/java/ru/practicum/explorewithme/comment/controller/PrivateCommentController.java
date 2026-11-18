package ru.practicum.explorewithme.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.comment.CommentService;
import ru.practicum.explorewithme.comment.dto.NewCommentDto;
import ru.practicum.explorewithme.comment.dto.CommentDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
@Validated
public class PrivateCommentController {
    private final CommentService commentService;

    @PostMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@PathVariable long userId, @PathVariable long eventId,
                             @Valid @RequestBody NewCommentDto dto) {
        return commentService.create(dto, userId, eventId);
    }

    @PatchMapping("/comments/{commentId}")
    public CommentDto update(@PathVariable long userId, @PathVariable long commentId,
                             @Valid @RequestBody NewCommentDto dto) {
        return commentService.update(dto, userId, commentId);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long userId, @PathVariable long commentId) {
        commentService.delete(userId, commentId);
    }

    @GetMapping("/comments")
    public List<CommentDto> getUserComments(@PathVariable long userId,
                                            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                            @RequestParam(defaultValue = "10") @Positive int size) {
        return commentService.getUserComments(userId, from, size);
    }
}
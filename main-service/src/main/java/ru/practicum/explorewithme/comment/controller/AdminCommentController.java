package ru.practicum.explorewithme.comment.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.comment.CommentService;
import ru.practicum.explorewithme.comment.dto.AdminCommentParams;
import ru.practicum.explorewithme.comment.dto.CommentDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
@Validated
public class AdminCommentController {
    private final CommentService service;

    @GetMapping
    public List<CommentDto> getCommentsByAdmin(@RequestParam(required = false) List<Long> users,
                                               @RequestParam(required = false) List<Long> events,
                                               @RequestParam(required = false) String text,
                                               @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                               @RequestParam(defaultValue = "10") @Positive int size) {
        return service.getCommentsByAdmin(new AdminCommentParams(users, events, text, from, size));
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByAdmin(@PathVariable Long commentId) {
        service.deleteByAdmin(commentId);
    }
}

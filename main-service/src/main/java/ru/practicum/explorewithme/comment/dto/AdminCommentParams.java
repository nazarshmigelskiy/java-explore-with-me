package ru.practicum.explorewithme.comment.dto;

import java.util.List;

public record AdminCommentParams(List<Long> users,
                                 List<Long> events,
                                 String text,
                                 int from, int size) {
}

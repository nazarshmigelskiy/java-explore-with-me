package ru.practicum.explorewithme.comment;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

@UtilityClass
public class CommentSpecification {

    public static Specification<Comment> authorIn(List<Long> users) {
        return (root, query, cb) -> users == null || users.isEmpty()
                ? null
                : root.get("author").get("id").in(users);
    }

    public static Specification<Comment> eventIn(List<Long> events) {
        return (root, query, cb) -> events == null || events.isEmpty()
                ? null
                : root.get("event").get("id").in(events);
    }

    public static Specification<Comment> textContains(String text) {
        return (root, query, cb) -> text == null || text.isBlank()
                ? null
                : cb.like(cb.lower(root.get("text")), "%" + text.toLowerCase() + "%");
    }
}

package ru.practicum.explorewithme.event;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.explorewithme.event.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;

@UtilityClass
public class EventSpecification {

    public static Specification<Event> initiatorIn(List<Long> users) {
        return (root, query, cb) -> users == null || users.isEmpty()
                ? null
                : root.get("initiator").get("id").in(users);
    }

    public static Specification<Event> stateIn(List<EventState> states) {
        return (root, query, cb) -> states == null || states.isEmpty()
                ? null
                : root.get("state").in(states);
    }

    public static Specification<Event> categoryIn(List<Long> categories) {
        return (root, query, cb) -> categories == null || categories.isEmpty()
                ? null
                : root.get("category").get("id").in(categories);
    }

    public static Specification<Event> eventDateFrom(LocalDateTime start) {
        return (root, query, cb) -> start == null
                ? null
                : cb.greaterThanOrEqualTo(root.<LocalDateTime>get("eventDate"), start);
    }

    public static Specification<Event> eventDateTo(LocalDateTime end) {
        return (root, query, cb) -> end == null
                ? null
                : cb.lessThanOrEqualTo(root.<LocalDateTime>get("eventDate"), end);
    }
}

package ru.practicum.explorewithme.event;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.explorewithme.event.enums.EventState;
import ru.practicum.explorewithme.request.ParticipationRequest;
import ru.practicum.explorewithme.request.RequestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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

    public static Specification<Event> textContains(String text) {
        return (root, query, cb) -> {
            if (text == null || text.isBlank()) {
                return null;
            }
            String pattern = "%" + text.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("annotation")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern));
        };
    }

    public static Specification<Event> paidEquals(Boolean paid) {
        return (root, query, cb)
                -> paid == null ? null : cb.equal(root.get("paid"), paid);
    }

    public static Specification<Event> onlyAvailable(boolean onlyAvailable) {
        return (root, query, cb) -> {
            if (!onlyAvailable) {
                return null;
            }
            Objects.requireNonNull(query, "Фильтр onlyAvailable работает только в запросах на выборку");
            Subquery<Long> confirmed = query.subquery(Long.class);
            Root<ParticipationRequest> request = confirmed.from(ParticipationRequest.class);
            confirmed.select(cb.count(request))
                    .where(cb.equal(request.get("event"), root),
                            cb.equal(request.get("status"), RequestStatus.CONFIRMED));
            return cb.or(
                    cb.equal(root.get("participantLimit"), 0),
                    cb.greaterThan(root.get("participantLimit").as(Long.class), confirmed));
        };
    }
}

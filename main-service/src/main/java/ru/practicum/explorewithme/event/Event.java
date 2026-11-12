package ru.practicum.explorewithme.event;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.explorewithme.category.Category;
import ru.practicum.explorewithme.event.enums.EventState;
import ru.practicum.explorewithme.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String annotation;
    private String description;
    private String title;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id")
    private User initiator;
    @Embedded
    private Location location;
    private LocalDateTime createdOn;
    private LocalDateTime eventDate;
    private LocalDateTime publishedOn;
    private Boolean paid;
    private Boolean requestModeration;
    private Integer participantLimit;
    @Enumerated(EnumType.STRING)
    private EventState state;
}

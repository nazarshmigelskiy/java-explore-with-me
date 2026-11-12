package ru.practicum.explorewithme.event.dto;


import lombok.*;
import ru.practicum.explorewithme.event.enums.UserStateAction;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventUserRequest extends UpdateEventRequest {
    private UserStateAction stateAction;
}

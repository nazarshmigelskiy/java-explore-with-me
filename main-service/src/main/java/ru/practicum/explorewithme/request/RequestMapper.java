package ru.practicum.explorewithme.request;

import lombok.experimental.UtilityClass;
import ru.practicum.explorewithme.request.dto.ParticipationRequestDto;

@UtilityClass
public class RequestMapper {
    public static ParticipationRequestDto toDto(ParticipationRequest r) {
        return new ParticipationRequestDto(
                r.getId(),
                r.getCreated(),
                r.getEvent().getId(),
                r.getRequester().getId(),
                r.getStatus()
        );
    }
}

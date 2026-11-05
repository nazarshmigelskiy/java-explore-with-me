package ru.practicum.explorewithme.mapper;

import ru.practicum.explorewithme.dto.EndpointHitDto;
import ru.practicum.explorewithme.model.EndpointHit;

public class EndpointHitMapper {

    private EndpointHitMapper() {

    }

    public static EndpointHit toEndpointHit(EndpointHitDto dto) {
        return new EndpointHit(
                null,
                dto.getApp(),
                dto.getUri(),
                dto.getIp(),
                dto.getTimestamp()
        );
    }
}

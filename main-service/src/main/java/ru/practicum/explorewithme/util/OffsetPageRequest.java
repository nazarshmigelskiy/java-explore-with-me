package ru.practicum.explorewithme.util;

import lombok.EqualsAndHashCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@EqualsAndHashCode(callSuper = true)
public class OffsetPageRequest extends PageRequest {
    private final int from;

    public OffsetPageRequest(int from, int size, Sort sort) {
        super(from / size, size, sort);
        this.from = from;
    }

    @Override
    public long getOffset() {
        return from;
    }
}

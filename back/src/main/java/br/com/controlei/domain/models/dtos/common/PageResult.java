package br.com.controlei.domain.models.dtos.common;

import java.util.List;
import java.util.function.Function;

public record PageResult<T>(
        List<T> content,
        int number,
        int size,
        long totalElements,
        int totalPages
) {
    public <R> PageResult<R> map(Function<T, R> mapper) {
        List<R> mappedContent = content.stream().map(mapper).toList();
        return new PageResult<>(mappedContent, number, size, totalElements, totalPages);
    }
}

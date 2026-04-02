package com.fesi.deadlinemate.domain.gathering.dto.request;


import com.fesi.deadlinemate.domain.gathering.entity.GatheringType;
import java.util.List;
import java.util.Objects;
import lombok.Builder;

@Builder
public record GatheringSearchCondition(
        GatheringType type,
        List<Long> categoryIds,
        String sort,
        String status,
        String query
) {
    public String normalizedSort() {
        return (sort == null || sort.isBlank()) ? "latest" : sort.toLowerCase();
    }

    public String normalizedStatus() {
        return (status == null || status.isBlank()) ? "recruiting" : status.toLowerCase();
    }

    public List<Long> normalizedCategoryIds() {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }
        return categoryIds.stream().filter(Objects::nonNull).distinct().toList();
    }

    public String normalizedQuery() {
        return (query == null || query.isBlank()) ? null : query.trim();
    }
}

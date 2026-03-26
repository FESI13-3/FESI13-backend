package com.fesi.deadlinemate.domain.gathering.dto.request;


import com.fesi.deadlinemate.domain.gathering.entity.GatheringType;
import lombok.Builder;

@Builder
public record GatheringSearchCondition(
        GatheringType type,
        String category,
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

    public String normalizedCategory() {
        return (category == null || category.isBlank()) ? null : category.trim();
    }

    public String normalizedQuery() {
        return (query == null || query.isBlank()) ? null : query.trim();
    }
}

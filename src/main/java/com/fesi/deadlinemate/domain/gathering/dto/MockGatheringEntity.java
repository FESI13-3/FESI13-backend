package com.fesi.deadlinemate.domain.gathering.dto;

import com.fesi.deadlinemate.domain.gathering.dto.MockGatheringDtos.WeeklyGuideRequest;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MockGatheringEntity {
    public Long id;
    public String type;
    public String category;
    public String title;
    public String shortDescription;
    public String description;
    public List<String> tags = new ArrayList<>();
    public String goal;
    public Integer maxMembers;
    public Integer currentMembers;
    public LocalDate recruitDeadline;
    public LocalDate startDate;
    public LocalDate endDate;
    public String status;
    public Long leaderId;
    public List<WeeklyGuideRequest> weeklyGuides = new ArrayList<>();
    public List<Map<String, Object>> images = new ArrayList<>();
    public OffsetDateTime createdAt;
}

package com.fesi.deadlinemate.domain.gathering.entity;

import com.fesi.deadlinemate.domain.gathering.command.UpdateGatheringCommand;
import com.fesi.deadlinemate.global.common.BaseTimeEntity;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gatherings", indexes = {
        @Index(name = "idx_gatherings_status_created_at", columnList = "status, createdAt"),
        @Index(name = "idx_gatherings_recruit_deadline", columnList = "recruitDeadline"),
        @Index(name = "idx_gatherings_view_count", columnList = "viewCount"),
        @Index(name = "idx_gatherings_type_category", columnList = "type, category")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Gathering extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long leaderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GatheringType type;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 60)
    private String title;

    @Column(nullable = false, length = 100)
    private String shortDescription;

    @Lob
    @Column(nullable = false)
    private String description;

    @Lob
    @Column(nullable = false)
    private String goal;

    @Column(nullable = false)
    private int maxMembers;

    @Column(nullable = false)
    private int currentMembers;

    @Column(nullable = false)
    private LocalDate recruitDeadline;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private int totalWeeks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GatheringStatus status;

    @Column(nullable = false)
    private int viewCount;

    @Builder
    public Gathering(
            Long leaderId,
            GatheringType type,
            String category,
            String title,
            String shortDescription,
            String description,
            String goal,
            int maxMembers,
            int currentMembers,
            LocalDate recruitDeadline,
            LocalDate startDate,
            LocalDate endDate,
            int totalWeeks,
            GatheringStatus status,
            int viewCount
    ) {
        this.leaderId = leaderId;
        this.type = type;
        this.category = category;
        this.title = title;
        this.shortDescription = shortDescription;
        this.description = description;
        this.goal = goal;
        this.maxMembers = maxMembers;
        this.currentMembers = currentMembers;
        this.recruitDeadline = recruitDeadline;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalWeeks = totalWeeks;
        this.status = status;
        this.viewCount = viewCount;
    }

    public void updateRecruitingInfo(
            GatheringType type,
            String category,
            String title,
            String shortDescription,
            String description,
            String goal,
            int maxMembers,
            LocalDate recruitDeadline,
            LocalDate startDate,
            LocalDate endDate,
            int totalWeeks
    ) {
        this.type = type;
        this.category = category;
        this.title = title;
        this.shortDescription = shortDescription;
        this.description = description;
        this.goal = goal;
        this.maxMembers = maxMembers;
        this.recruitDeadline = recruitDeadline;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalWeeks = totalWeeks;
    }

    public void updateInProgressInfo(
            String description,
            LocalDate endDate,
            int totalWeeks
    ) {
        this.description = description;
        this.endDate = endDate;
        this.totalWeeks = totalWeeks;
    }
}
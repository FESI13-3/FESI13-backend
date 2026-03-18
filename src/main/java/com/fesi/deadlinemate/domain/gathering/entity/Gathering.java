package com.fesi.deadlinemate.domain.gathering.entity;

import com.fesi.deadlinemate.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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

    @Column(name = "leader_id", nullable = false)
    private Long leaderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GatheringType type;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 60)
    private String title;

    @Column(name = "short_description", nullable = false, length = 100)
    private String shortDescription;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String goal;

    @Column(name = "max_members", nullable = false)
    private Integer maxMembers;

    @Column(name = "current_members", nullable = false)
    private Integer currentMembers;

    @Column(name = "recruit_deadline", nullable = false)
    private LocalDate recruitDeadline;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_weeks", nullable = false)
    private Integer totalWeeks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GatheringStatus status;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Builder
    public Gathering(Long leaderId,
                     GatheringType type,
                     String category,
                     String title,
                     String shortDescription,
                     String description,
                     String goal,
                     Integer maxMembers,
                     LocalDate recruitDeadline,
                     LocalDate startDate,
                     LocalDate endDate,
                     Integer totalWeeks) {
        this.leaderId = leaderId;
        this.type = type;
        this.category = category;
        this.title = title;
        this.shortDescription = shortDescription;
        this.description = description;
        this.goal = goal;
        this.maxMembers = maxMembers;
        this.currentMembers = 1;
        this.recruitDeadline = recruitDeadline;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalWeeks = totalWeeks;
        this.status = GatheringStatus.RECRUITING;
        this.viewCount = 0;
    }
}

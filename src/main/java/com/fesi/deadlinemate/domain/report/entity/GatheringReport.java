package com.fesi.deadlinemate.domain.report.entity;

import com.fesi.deadlinemate.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gathering_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GatheringReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long gatheringId;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal teamOverallRate;

    private Long mvpUserId;

    private Long longestStreakUserId;

    private Long mostImprovedUserId;

    private Long attendanceUserId;

    @Lob
    @Column(nullable = false, columnDefinition = "json")
    private String weeklyRates;

    @Builder
    public GatheringReport(
            Long gatheringId,
            BigDecimal teamOverallRate,
            Long mvpUserId,
            Long longestStreakUserId,
            Long mostImprovedUserId,
            Long attendanceUserId,
            String weeklyRates
    ) {
        this.gatheringId = gatheringId;
        this.teamOverallRate = teamOverallRate;
        this.mvpUserId = mvpUserId;
        this.longestStreakUserId = longestStreakUserId;
        this.mostImprovedUserId = mostImprovedUserId;
        this.attendanceUserId = attendanceUserId;
        this.weeklyRates = weeklyRates;
    }
}

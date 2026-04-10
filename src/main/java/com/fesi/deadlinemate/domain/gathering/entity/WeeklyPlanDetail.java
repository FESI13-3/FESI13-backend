package com.fesi.deadlinemate.domain.gathering.entity;

import com.fesi.deadlinemate.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "weekly_plan_details",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_weekly_plan_details_order",
                columnNames = {"weekly_plan_id", "display_order"}
        ),
        indexes = {
                @Index(name = "idx_weekly_plan_details_plan_order", columnList = "weekly_plan_id, display_order")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyPlanDetail extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "weekly_plan_id", nullable = false)
    private Long weeklyPlanId;

    @Column(nullable = false)
    private Integer displayOrder;

    @Column(nullable = false, length = 200)
    private String content;

    @Builder
    public WeeklyPlanDetail(Long weeklyPlanId, int displayOrder, String content) {
        this.weeklyPlanId = weeklyPlanId;
        this.displayOrder = displayOrder;
        this.content = content;
    }
}

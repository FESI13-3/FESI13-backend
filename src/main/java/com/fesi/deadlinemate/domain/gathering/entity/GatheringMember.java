package com.fesi.deadlinemate.domain.gathering.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gathering_members",
        uniqueConstraints = @UniqueConstraint(name = "uk_gathering_members_user", columnNames = {"gatheringId", "userId"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GatheringMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long gatheringId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GatheringRole role;

    @Lob
    private String personalGoal;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal overallAchievementRate;

    @Column(nullable = false)
    private boolean isActive;

    @Builder
    public GatheringMember(Long gatheringId, Long userId, GatheringRole role,
                           String personalGoal, BigDecimal overallAchievementRate, boolean isActive) {
        this.gatheringId = gatheringId;
        this.userId = userId;
        this.role = role;
        this.personalGoal = personalGoal;
        this.overallAchievementRate = overallAchievementRate;
        this.isActive = isActive;
    }

    public boolean isLeader() {
        return this.role == GatheringRole.LEADER;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
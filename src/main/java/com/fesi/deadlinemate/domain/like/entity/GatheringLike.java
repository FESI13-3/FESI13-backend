package com.fesi.deadlinemate.domain.like.entity;

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
        name = "gathering_likes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_gathering_likes_user",
                        columnNames = {"gatheringId", "userId"}
                )
        },
        indexes = {
                @Index(name = "idx_gathering_likes_user_created_at", columnList = "userId, createdAt")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GatheringLike extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long gatheringId;

    @Column(nullable = false)
    private Long userId;

    @Builder
    public GatheringLike(Long gatheringId, Long userId) {
        this.gatheringId = gatheringId;
        this.userId = userId;
    }
}
package com.fesi.deadlinemate.domain.review.entity;

import com.fesi.deadlinemate.global.common.BaseTimeEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews",
        indexes = {
                @Index(name = "idx_reviews_target_user_id_created_at", columnList = "targetUserId, createdAt")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_reviews_gathering_reviewer_target",
                        columnNames = {"gatheringId", "reviewerId", "targetUserId"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long gatheringId;

    @Column(nullable = false)
    private Long reviewerId;

    @Column(nullable = false)
    private Long targetUserId;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "review_tags", joinColumns = @JoinColumn(name = "review_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "tag", nullable = false, length = 30)
    private List<ReviewTag> tags;

    @Column(length = 50)
    private String matesTag;

    @Column(length = 300)
    private String comment;

    @Builder(access = AccessLevel.PRIVATE)
    private Review(Long gatheringId, Long reviewerId, Long targetUserId,
                   List<ReviewTag> tags, String matesTag, String comment) {
        this.gatheringId = gatheringId;
        this.reviewerId = reviewerId;
        this.targetUserId = targetUserId;
        this.tags = tags;
        this.matesTag = matesTag;
        this.comment = comment;
    }

    public static Review create(Long gatheringId, Long reviewerId, Long targetUserId,
                                List<String> tagDisplayNames, String matesTag, String comment) {
        List<ReviewTag> tags = tagDisplayNames.stream()
                .map(ReviewTag::fromDisplayName)
                .toList();

        return Review.builder()
                .gatheringId(gatheringId)
                .reviewerId(reviewerId)
                .targetUserId(targetUserId)
                .tags(tags)
                .matesTag(matesTag)
                .comment(comment)
                .build();
    }

    public int getTagCount() {
        return this.tags.size();
    }
}

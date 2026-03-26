package com.fesi.deadlinemate.domain.gathering.entity;

import com.fesi.deadlinemate.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gathering_images",
        uniqueConstraints = @UniqueConstraint(name = "uk_gathering_images_order", columnNames = {"gatheringId", "displayOrder"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GatheringImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long gatheringId;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private int displayOrder;

    @Builder
    public GatheringImage(Long gatheringId, String imageUrl, int displayOrder) {
        this.gatheringId = gatheringId;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
    }
}

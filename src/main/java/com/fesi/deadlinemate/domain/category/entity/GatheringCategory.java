package com.fesi.deadlinemate.domain.category.entity;

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
        name = "gathering_categories",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_gathering_categories",
                columnNames = {"gatheringId", "categoryId"}
        ),
        indexes = {
                @Index(name = "idx_gathering_categories_category_id_gathering_id", columnList = "categoryId, gatheringId")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GatheringCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long gatheringId;

    @Column(nullable = false)
    private Long categoryId;

    @Builder
    public GatheringCategory(Long gatheringId, Long categoryId) {
        this.gatheringId = gatheringId;
        this.categoryId = categoryId;
    }
}

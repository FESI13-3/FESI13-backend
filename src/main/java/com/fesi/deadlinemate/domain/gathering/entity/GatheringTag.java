package com.fesi.deadlinemate.domain.gathering.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gathering_tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GatheringTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gathering_id", nullable = false)
    private Long gatheringId;

    @Column(nullable = false, length = 30)
    private String tag;

    public GatheringTag(Long gatheringId, String tag) {
        this.gatheringId = gatheringId;
        this.tag = tag;
    }
}

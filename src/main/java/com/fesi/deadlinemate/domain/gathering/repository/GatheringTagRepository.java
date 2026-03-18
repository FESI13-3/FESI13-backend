package com.fesi.deadlinemate.domain.gathering.repository;

import com.fesi.deadlinemate.domain.gathering.entity.GatheringTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringTagRepository extends JpaRepository<GatheringTag,Long> {
}

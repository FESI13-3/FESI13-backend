package com.fesi.deadlinemate.domain.review.client;

import com.fesi.deadlinemate.domain.review.client.dto.ApplicantReviewInfo;
import java.util.List;
import java.util.Map;

public interface ReviewClient {
    Map<Long, ApplicantReviewInfo> getApplicantReviewInfos(List<Long> targetUserIds);
}

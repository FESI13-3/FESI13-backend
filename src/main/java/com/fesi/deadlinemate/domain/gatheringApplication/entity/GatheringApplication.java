package com.fesi.deadlinemate.domain.gatheringApplication.entity;

import com.fesi.deadlinemate.global.common.BaseTimeEntity;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "applications",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_applications_gathering_applicant",
                columnNames = {"gatheringId", "applicantId"}
        ),
        indexes = {
                @Index(name = "idx_applications_gathering_id", columnList = "gatheringId"),
                @Index(name = "idx_applications_applicant_id", columnList = "applicantId")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GatheringApplication extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long gatheringId;

    @Column(nullable = false)
    private Long applicantId;

    @Lob
    @Column(nullable = false)
    private String personalGoal;

    @Lob
    private String selfIntroduction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    @Builder
    public GatheringApplication(
            Long gatheringId,
            Long applicantId,
            String personalGoal,
            String selfIntroduction,
            ApplicationStatus status
    ) {
        this.gatheringId = gatheringId;
        this.applicantId = applicantId;
        this.personalGoal = personalGoal;
        this.selfIntroduction = selfIntroduction;
        this.status = status;
    }

    public void accept() {
        validatePending();
        this.status = ApplicationStatus.ACCEPTED;
    }

    public void reject() {
        validatePending();
        this.status = ApplicationStatus.REJECTED;
    }

    public void validateCancelableBy(Long requesterId) {
        if (!this.applicantId.equals(requesterId)) {
            throw new BusinessException(ErrorCode.APPLICATION_CANCEL_FORBIDDEN);
        }
        validatePending();
    }

    public boolean isPending() {
        return this.status == ApplicationStatus.PENDING;
    }

    private void validatePending() {
        if (!isPending()) {
            throw new BusinessException(ErrorCode.INVALID_APPLICATION_STATUS_CHANGE);
        }
    }
}

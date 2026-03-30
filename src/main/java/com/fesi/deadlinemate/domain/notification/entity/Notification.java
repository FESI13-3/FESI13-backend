package com.fesi.deadlinemate.domain.notification.entity;

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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_user_id_created_at", columnList = "userId, createdAt"),
        @Index(name = "idx_notifications_user_id_is_read", columnList = "userId, isRead")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String content;

    @Column(nullable = false)
    private boolean isRead;

    @Column(length = 200)
    private String targetUrl;

    private Long referenceId;

    @Column(length = 30)
    private String referenceType;

    @Builder
    public Notification(Long userId, NotificationType type, String content,
                        String targetUrl, Long referenceId, String referenceType) {
        this.userId = userId;
        this.type = type;
        this.content = content;
        this.targetUrl = targetUrl;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.isRead = false;
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public void validateOwnership(Long requesterId) {
        if (!this.userId.equals(requesterId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}

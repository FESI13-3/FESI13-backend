package com.fesi.deadlinemate.domain.user.entity;

import com.fesi.deadlinemate.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_nickname", columnList = "nickname"),
        @Index(name = "idx_users_provider_provider_id", columnList = "provider, providerId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    @Column(columnDefinition = "TEXT")
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column
    private String providerId;

    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal reputationScore;

    @Column(nullable = false)
    private boolean isActive;

    @Builder
    public User(String email, String passwordHash, String nickname, String profileImage,
                Provider provider, String providerId) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.provider = provider;
        this.providerId = providerId;
        this.reputationScore = BigDecimal.valueOf(36.5);
        this.isActive = true;
    }

    public void updateProfile(String nickname, String profileImage) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
    }

    public void updatePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean isEmailUser() {
        return this.provider == Provider.EMAIL;
    }

    public void addReputationScore(java.math.BigDecimal delta) {
        this.reputationScore = this.reputationScore.add(delta);
    }
}

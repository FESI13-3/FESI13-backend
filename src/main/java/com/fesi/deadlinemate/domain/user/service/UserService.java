package com.fesi.deadlinemate.domain.user.service;

import com.fesi.deadlinemate.domain.user.entity.Provider;
import com.fesi.deadlinemate.domain.user.entity.User;
import com.fesi.deadlinemate.domain.user.repository.UserRepository;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createEmailUser(String email, String password, String nickname) {
        validateEmailNotExists(email);
        validateNicknameNotExists(nickname);

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .nickname(nickname)
                .provider(Provider.EMAIL)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User findOrCreateOAuthUser(String email, String nickname, String profileImage,
                                      Provider provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    String uniqueNickname = ensureUniqueNickname(nickname);
                    User user = User.builder()
                            .email(email)
                            .nickname(uniqueNickname)
                            .profileImage(profileImage)
                            .provider(provider)
                            .providerId(providerId)
                            .build();
                    return userRepository.save(user);
                });
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public boolean existsByProviderAndProviderId(Provider provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId).isPresent();
    }

    @Transactional
    public User updateProfile(Long userId, String nickname, String profileImage) {
        User user = findById(userId);

        if (nickname != null && !nickname.equals(user.getNickname())) {
            validateNicknameNotExists(nickname);
        }

        user.updateProfile(nickname, profileImage);
        return user;
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = findById(userId);

        if (!user.isEmailUser()) {
            throw new BusinessException(ErrorCode.SOCIAL_USER_PASSWORD_CHANGE);
        }

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_NOT_MATCHED);
        }

        user.updatePassword(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public void deactivate(Long userId) {
        User user = findById(userId);
        user.deactivate();
    }

    public List<User> findByIds(List<Long> userIds) {
        return userRepository.findByIdIn(userIds);
    }

    private void validateEmailNotExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    private void validateNicknameNotExists(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
    }

    private String ensureUniqueNickname(String nickname) {
        if (!userRepository.existsByNickname(nickname)) {
            return nickname;
        }
        int suffix = 1;
        String candidate;
        do {
            candidate = nickname + suffix++;
        } while (userRepository.existsByNickname(candidate));
        return candidate;
    }
}

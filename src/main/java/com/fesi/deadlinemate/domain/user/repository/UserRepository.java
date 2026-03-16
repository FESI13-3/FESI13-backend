package com.fesi.deadlinemate.domain.user.repository;

import com.fesi.deadlinemate.domain.user.entity.Provider;
import com.fesi.deadlinemate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}

package com.fesi.deadlinemate.domain.auth.provider;

import com.fesi.deadlinemate.domain.user.entity.User;

public interface AuthProvider {

    User authenticate(String identifier, String credential);
}

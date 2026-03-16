package com.fesi.deadlinemate.domain.auth.client;

import com.fesi.deadlinemate.domain.user.entity.Provider;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OAuthClientFactory {

    private final Map<Provider, OAuthClient> clients;

    public OAuthClientFactory(List<OAuthClient> oAuthClients) {
        this.clients = oAuthClients.stream()
                .collect(Collectors.toMap(OAuthClient::getProvider, Function.identity()));
    }

    public OAuthClient getClient(Provider provider) {
        OAuthClient client = clients.get(provider);
        if (client == null) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        }
        return client;
    }
}

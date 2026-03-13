package org.example.security;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionTokenManager {
    private static final long TOKEN_TTL_SECONDS = 60 * 60 * 8;

    private static class SessionInfo {
        private final int userId;
        private final Instant expiresAt;

        private SessionInfo(int userId, Instant expiresAt) {
            this.userId = userId;
            this.expiresAt = expiresAt;
        }
    }

    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();

    public String issueToken(int userId) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, new SessionInfo(userId, Instant.now().plusSeconds(TOKEN_TTL_SECONDS)));
        return token;
    }

    public Optional<Integer> resolveUser(String token) {
        SessionInfo info = sessions.get(token);
        if (info == null) {
            return Optional.empty();
        }
        if (Instant.now().isAfter(info.expiresAt)) {
            sessions.remove(token);
            return Optional.empty();
        }
        return Optional.of(info.userId);
    }
}

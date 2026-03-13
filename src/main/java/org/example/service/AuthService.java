package org.example.service;

import org.example.model.User;
import org.example.security.SessionTokenManager;
import org.example.store.InMemorySchoolStore;

import java.util.Optional;

public class AuthService {
    private final InMemorySchoolStore store;
    private final SessionTokenManager tokenManager;

    public AuthService(InMemorySchoolStore store, SessionTokenManager tokenManager) {
        this.store = store;
        this.tokenManager = tokenManager;
    }

    public Optional<String> login(String username, String password) {
        return store.findByUsername(username)
                .filter(user -> user.getPassword().equals(password))
                .map(user -> tokenManager.issueToken(user.getId()));
    }

    public Optional<User> resolveByToken(String token) {
        return tokenManager.resolveUser(token)
                .flatMap(store::findUser);
    }
}

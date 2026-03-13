package org.example.bootstrap;

import org.example.security.SessionTokenManager;
import org.example.service.AuthService;
import org.example.service.SchoolService;
import org.example.store.InMemorySchoolStore;

public final class AppContext {
    private static final InMemorySchoolStore STORE = new InMemorySchoolStore();
    private static final SessionTokenManager TOKEN_MANAGER = new SessionTokenManager();
    private static final SchoolService SCHOOL_SERVICE = new SchoolService(STORE);
    private static final AuthService AUTH_SERVICE = new AuthService(STORE, TOKEN_MANAGER);

    static {
        DataBootstrap.seed(SCHOOL_SERVICE, STORE);
    }

    private AppContext() {
    }

    public static SchoolService schoolService() {
        return SCHOOL_SERVICE;
    }

    public static AuthService authService() {
        return AUTH_SERVICE;
    }
}

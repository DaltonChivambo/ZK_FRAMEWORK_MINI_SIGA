package org.example.bootstrap;

import org.example.security.SessionTokenManager;
import org.example.service.AuthService;
import org.example.service.SchoolService;
import org.example.store.DbConfig;
import org.example.store.JdbcSchoolStore;
import org.example.store.SchoolStore;

public final class AppContext {
    private static final SchoolStore STORE = new JdbcSchoolStore(
            DbConfig.url(),
            DbConfig.user(),
            DbConfig.password()
    );
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

package org.example.rest;

import org.example.bootstrap.AppContext;
import org.example.model.Role;
import org.example.model.User;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

public final class ApiSecurity {
    private ApiSecurity() {
    }

    public static User requireUser(HttpHeaders headers) {
        String token = headers.getHeaderString("X-Auth-Token");
        if (token == null || token.isBlank()) {
            throw new WebApplicationException("Token ausente.", Response.Status.UNAUTHORIZED);
        }
        return AppContext.authService().resolveByToken(token)
                .orElseThrow(() -> new WebApplicationException("Token invalido.", Response.Status.UNAUTHORIZED));
    }

    public static User requireRole(HttpHeaders headers, Role role) {
        User user = requireUser(headers);
        if (user.getRole() != role) {
            throw new WebApplicationException("Sem permissao.", Response.Status.FORBIDDEN);
        }
        return user;
    }
}

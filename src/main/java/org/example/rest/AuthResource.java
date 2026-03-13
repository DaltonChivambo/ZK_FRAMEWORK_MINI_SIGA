package org.example.rest;

import org.example.bootstrap.AppContext;
import org.example.dto.AuthRequest;
import org.example.dto.AuthResponse;
import org.example.model.User;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Optional;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    @POST
    @Path("/login")
    public Response login(AuthRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Credenciais invalidas.").build();
        }

        Optional<String> maybeToken = AppContext.authService().login(request.getUsername(), request.getPassword());
        if (maybeToken.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Utilizador ou senha incorretos.").build();
        }

        User user = AppContext.authService().resolveByToken(maybeToken.get()).orElseThrow();
        AuthResponse response = new AuthResponse(maybeToken.get(), user.getId(), user.getNome(), user.getRole());
        return Response.ok(response).build();
    }
}

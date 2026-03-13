package org.example.rest;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof WebApplicationException) {
            WebApplicationException wae = (WebApplicationException) exception;
            return Response.status(wae.getResponse().getStatus())
                    .entity(Map.of("error", exception.getMessage()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        if (exception instanceof IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", exception.getMessage()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Erro interno no servidor."))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

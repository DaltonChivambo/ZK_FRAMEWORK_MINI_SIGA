package org.example.rest;

import org.example.bootstrap.AppContext;
import org.example.model.Role;
import org.example.model.User;
import org.example.service.SchoolService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/student")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StudentResource {
    private final SchoolService schoolService = AppContext.schoolService();

    @GET
    @Path("/grades")
    public Response myGrades(@Context HttpHeaders headers) {
        User estudante = ApiSecurity.requireRole(headers, Role.ESTUDANTE);
        return Response.ok(schoolService.listarNotasEstudante(estudante.getId())).build();
    }

    @GET
    @Path("/{studentId}/grades")
    public Response studentGradesById(@Context HttpHeaders headers, @PathParam("studentId") int studentId) {
        User user = ApiSecurity.requireUser(headers);
        if (user.getRole() == Role.ESTUDANTE && user.getId() != studentId) {
            throw new WebApplicationException("Sem permissao para ver notas de outro estudante.", Response.Status.FORBIDDEN);
        }
        return Response.ok(schoolService.listarNotasEstudante(studentId)).build();
    }
}

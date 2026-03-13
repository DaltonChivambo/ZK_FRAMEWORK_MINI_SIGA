package org.example.rest;

import org.example.bootstrap.AppContext;
import org.example.dto.PublishGradeRequest;
import org.example.model.Role;
import org.example.model.User;
import org.example.service.SchoolService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/teacher")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TeacherResource {
    private final SchoolService schoolService = AppContext.schoolService();

    @POST
    @Path("/grades")
    public Response publishGrade(@Context HttpHeaders headers, PublishGradeRequest request) {
        User professor = ApiSecurity.requireRole(headers, Role.PROFESSOR);
        if (request == null || request.getAvaliacao() == null || request.getAvaliacao().isBlank()) {
            throw new WebApplicationException("Dados da nota invalidos.", Response.Status.BAD_REQUEST);
        }

        return Response.ok(schoolService.publicarNota(
                professor.getId(),
                request.getStudentId(),
                request.getSubjectId(),
                request.getAvaliacao(),
                request.getNota()
        )).build();
    }

    @GET
    @Path("/subjects")
    public Response mySubjects(@Context HttpHeaders headers) {
        User professor = ApiSecurity.requireRole(headers, Role.PROFESSOR);
        return Response.ok(Map.of(
                "professor", professor.getNome(),
                "subjects", schoolService.listarDisciplinas().stream()
                        .filter(s -> professor.getId() == (s.getTeacherId() == null ? -1 : s.getTeacherId()))
                        .toArray()
        )).build();
    }
}

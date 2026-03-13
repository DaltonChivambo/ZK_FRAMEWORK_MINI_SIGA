package org.example.rest;

import org.example.bootstrap.AppContext;
import org.example.dto.AssignmentRequest;
import org.example.dto.RegisterUserRequest;
import org.example.model.Course;
import org.example.model.Role;
import org.example.model.Subject;
import org.example.model.User;
import org.example.service.SchoolService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource {
    private final SchoolService schoolService = AppContext.schoolService();

    @POST
    @Path("/students")
    public Response createStudent(@Context HttpHeaders headers, RegisterUserRequest request) {
        ApiSecurity.requireRole(headers, Role.ADMIN);
        validarUserRequest(request);
        return Response.ok(schoolService.registrarEstudante(request.getNome(), request.getUsername(), request.getPassword())).build();
    }

    @POST
    @Path("/teachers")
    public Response createTeacher(@Context HttpHeaders headers, RegisterUserRequest request) {
        ApiSecurity.requireRole(headers, Role.ADMIN);
        validarUserRequest(request);
        return Response.ok(schoolService.registrarProfessor(request.getNome(), request.getUsername(), request.getPassword())).build();
    }

    @POST
    @Path("/courses")
    public Response createCourse(@Context HttpHeaders headers, Map<String, String> payload) {
        ApiSecurity.requireRole(headers, Role.ADMIN);
        String nome = payload == null ? null : payload.get("nome");
        if (nome == null || nome.isBlank()) {
            throw new WebApplicationException("Nome do curso e obrigatorio.", Response.Status.BAD_REQUEST);
        }
        Course course = schoolService.registrarCurso(nome);
        return Response.ok(course).build();
    }

    @POST
    @Path("/subjects")
    public Response createSubject(@Context HttpHeaders headers, Map<String, String> payload) {
        ApiSecurity.requireRole(headers, Role.ADMIN);
        if (payload == null || payload.get("nome") == null || payload.get("courseId") == null) {
            throw new WebApplicationException("Dados da disciplina invalidos.", Response.Status.BAD_REQUEST);
        }
        int courseId = Integer.parseInt(payload.get("courseId"));
        Subject subject = schoolService.registrarDisciplina(payload.get("nome"), courseId);
        return Response.ok(subject).build();
    }

    @POST
    @Path("/assign/student-course")
    public Response assignStudentToCourse(@Context HttpHeaders headers, AssignmentRequest request) {
        ApiSecurity.requireRole(headers, Role.ADMIN);
        schoolService.associarEstudanteAoCurso(request.getFirstId(), request.getSecondId());
        return Response.ok(Map.of("message", "Estudante associado ao curso com sucesso.")).build();
    }

    @POST
    @Path("/assign/teacher-subject")
    public Response assignTeacherToSubject(@Context HttpHeaders headers, AssignmentRequest request) {
        ApiSecurity.requireRole(headers, Role.ADMIN);
        schoolService.associarProfessorADisciplina(request.getFirstId(), request.getSecondId());
        return Response.ok(Map.of("message", "Professor associado a disciplina com sucesso.")).build();
    }

    @GET
    @Path("/overview")
    public Response overview(@Context HttpHeaders headers) {
        User admin = ApiSecurity.requireRole(headers, Role.ADMIN);
        return Response.ok(Map.of(
                "admin", admin.getNome(),
                "students", schoolService.listarEstudantes(),
                "teachers", schoolService.listarProfessores(),
                "courses", schoolService.listarCursos(),
                "subjects", schoolService.listarDisciplinas()
        )).build();
    }

    private void validarUserRequest(RegisterUserRequest request) {
        if (request == null ||
                request.getNome() == null || request.getNome().isBlank() ||
                request.getUsername() == null || request.getUsername().isBlank() ||
                request.getPassword() == null || request.getPassword().isBlank()) {
            throw new WebApplicationException("Dados de utilizador invalidos.", Response.Status.BAD_REQUEST);
        }
    }
}

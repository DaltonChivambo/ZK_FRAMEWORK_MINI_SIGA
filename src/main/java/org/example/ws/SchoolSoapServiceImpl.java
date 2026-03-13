package org.example.ws;

import org.example.bootstrap.AppContext;
import org.example.service.SchoolService;

import jakarta.jws.WebService;
import java.util.List;
import java.util.stream.Collectors;

@WebService(endpointInterface = "org.example.ws.SchoolSoapService", serviceName = "SchoolSoapService")
public class SchoolSoapServiceImpl implements SchoolSoapService {
    private final SchoolService schoolService = AppContext.schoolService();

    @Override
    public String listarNotasDoEstudante(int studentId) {
        List<SchoolService.GradeView> notas = schoolService.listarNotasEstudante(studentId);
        if (notas.isEmpty()) {
            return "[]";
        }
        return notas.stream()
                .map(g -> String.format(
                        "{\"avaliacao\":\"%s\",\"disciplina\":\"%s\",\"professor\":\"%s\",\"nota\":%.2f}",
                        escape(g.getAvaliacao()),
                        escape(g.getDisciplina()),
                        escape(g.getProfessor()),
                        g.getNota()
                ))
                .collect(Collectors.joining(",", "[", "]"));
    }

    private String escape(String value) {
        return value.replace("\"", "\\\"");
    }
}

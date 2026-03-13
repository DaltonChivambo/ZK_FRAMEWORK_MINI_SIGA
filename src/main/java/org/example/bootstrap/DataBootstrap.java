package org.example.bootstrap;

import org.example.model.Course;
import org.example.model.Student;
import org.example.model.Subject;
import org.example.model.Teacher;
import org.example.service.SchoolService;
import org.example.store.SchoolStore;

public final class DataBootstrap {
    private DataBootstrap() {
    }

    public static void seed(SchoolService schoolService, SchoolStore store) {
        if (store.hasAnyUsers()) {
            return;
        }

        store.createAdmin("Administrador", "admin", "admin123");

        Student s1 = schoolService.registrarEstudante("Ana Silva", "ana", "123456");
        Student s2 = schoolService.registrarEstudante("Bruno Costa", "bruno", "123456");
        Teacher t1 = schoolService.registrarProfessor("Prof. Marta", "marta", "123456");

        Course curso = schoolService.registrarCurso("Engenharia Informatica");
        Subject disciplina = schoolService.registrarDisciplina("Programacao Web", curso.getId());

        schoolService.associarEstudanteAoCurso(s1.getId(), curso.getId());
        schoolService.associarEstudanteAoCurso(s2.getId(), curso.getId());
        schoolService.associarProfessorADisciplina(t1.getId(), disciplina.getId());
        schoolService.publicarNota(t1.getId(), s1.getId(), disciplina.getId(), "Teste 1", 16.5);
    }
}

package org.example.service;

import org.example.model.*;
import org.example.store.SchoolStore;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SchoolService {
    private final SchoolStore store;

    public SchoolService(SchoolStore store) {
        this.store = store;
    }

    public Student registrarEstudante(String nome, String username, String password) {
        return store.createStudent(nome, username, password);
    }

    public Teacher registrarProfessor(String nome, String username, String password) {
        return store.createTeacher(nome, username, password);
    }

    public Course registrarCurso(String nome) {
        return store.createCourse(nome);
    }

    public Subject registrarDisciplina(String nome, int courseId) {
        validarCurso(courseId);
        return store.createSubject(nome, courseId);
    }

    public void associarEstudanteAoCurso(int studentId, int courseId) {
        store.findStudent(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Estudante nao encontrado."));
        validarCurso(courseId);
        store.assignStudentToCourse(studentId, courseId);
    }

    public void associarProfessorADisciplina(int teacherId, int subjectId) {
        Teacher teacher = store.findTeacher(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Professor nao encontrado."));
        Subject subject = store.findSubject(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Disciplina nao encontrada."));

        store.assignTeacherToSubject(teacherId, subjectId);
    }

    public GradeRecord publicarNota(int teacherId, int studentId, int subjectId, String avaliacao, double nota) {
        Teacher teacher = store.findTeacher(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Professor nao encontrado."));
        Student student = store.findStudent(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Estudante nao encontrado."));
        Subject subject = store.findSubject(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Disciplina nao encontrada."));

        if (!teacher.getSubjectIds().contains(subjectId)) {
            throw new IllegalArgumentException("Professor nao associado a disciplina.");
        }
        if (student.getCourseId() == null || student.getCourseId() != subject.getCourseId()) {
            throw new IllegalArgumentException("Estudante nao pertence ao curso da disciplina.");
        }
        if (nota < 0 || nota > 20) {
            throw new IllegalArgumentException("Nota invalida. Use intervalo de 0 a 20.");
        }

        return store.createGrade(studentId, subjectId, teacherId, avaliacao, nota);
    }

    public List<GradeView> listarNotasEstudante(int studentId) {
        return store.getGradesByStudent(studentId)
                .stream()
                .map(g -> new GradeView(
                        g.getId(),
                        g.getAvaliacao(),
                        g.getNota(),
                        store.findSubject(g.getSubjectId()).map(Subject::getNome).orElse("N/A"),
                        store.findTeacher(g.getTeacherId()).map(Teacher::getNome).orElse("N/A")
                ))
                .sorted(Comparator.comparing(GradeView::getDisciplina).thenComparing(GradeView::getAvaliacao))
                .collect(Collectors.toList());
    }

    public List<Student> listarEstudantes() {
        return store.getStudents();
    }

    public List<Teacher> listarProfessores() {
        return store.getTeachers();
    }

    public List<Course> listarCursos() {
        return store.getCourses();
    }

    public List<Subject> listarDisciplinas() {
        return store.getSubjects();
    }

    public Optional<User> buscarUsuario(int id) {
        return store.findUser(id);
    }

    private void validarCurso(int courseId) {
        if (store.findCourse(courseId).isEmpty()) {
            throw new IllegalArgumentException("Curso nao encontrado.");
        }
    }

    public static class GradeView {
        private final int id;
        private final String avaliacao;
        private final double nota;
        private final String disciplina;
        private final String professor;

        public GradeView(int id, String avaliacao, double nota, String disciplina, String professor) {
            this.id = id;
            this.avaliacao = avaliacao;
            this.nota = nota;
            this.disciplina = disciplina;
            this.professor = professor;
        }

        public int getId() {
            return id;
        }

        public String getAvaliacao() {
            return avaliacao;
        }

        public double getNota() {
            return nota;
        }

        public String getDisciplina() {
            return disciplina;
        }

        public String getProfessor() {
            return professor;
        }
    }
}

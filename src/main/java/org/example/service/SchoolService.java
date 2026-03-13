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
        Subject subject = store.createSubject(nome, courseId);
        // Inscricao automatica: todos estudantes do curso ficam inscritos na disciplina.
        listarEstudantesPorCurso(courseId)
                .forEach(student -> store.enrollStudentInSubject(student.getId(), subject.getId()));
        return subject;
    }

    public void associarEstudanteAoCurso(int studentId, int courseId) {
        store.findStudent(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Estudante nao encontrado."));
        validarCurso(courseId);
        store.assignStudentToCourse(studentId, courseId);
        // Reinscreve o estudante nas disciplinas do novo curso.
        store.clearStudentEnrollments(studentId);
        listarDisciplinasPorCurso(courseId)
                .forEach(subject -> store.enrollStudentInSubject(studentId, subject.getId()));
    }

    public void associarProfessorADisciplina(int teacherId, int subjectId) {
        Teacher teacher = store.findTeacher(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Professor nao encontrado."));
        Subject subject = store.findSubject(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Disciplina nao encontrada."));

        store.assignTeacherToSubject(teacherId, subjectId);
    }

    public void associarEstudanteADisciplina(int studentId, int subjectId) {
        Student student = store.findStudent(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Estudante nao encontrado."));
        Subject subject = store.findSubject(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Disciplina nao encontrada."));
        if (student.getCourseId() == null || student.getCourseId() != subject.getCourseId()) {
            throw new IllegalArgumentException("Estudante nao pertence ao curso da disciplina.");
        }
        store.enrollStudentInSubject(studentId, subjectId);
    }

    public GradeRecord publicarNota(int teacherId, int studentId, int subjectId, String avaliacao, double nota) {
        Assessment assessment = store.findAssessmentByName(subjectId, teacherId, avaliacao)
                .orElseGet(() -> criarAvaliacao(teacherId, subjectId, avaliacao));
        return publicarNotaPorAvaliacao(teacherId, assessment.getId(), studentId, nota);
    }

    public Assessment criarAvaliacao(int teacherId, int subjectId, String nome) {
        Teacher teacher = store.findTeacher(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Professor nao encontrado."));
        Subject subject = store.findSubject(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Disciplina nao encontrada."));
        if (!teacher.getSubjectIds().contains(subjectId)) {
            throw new IllegalArgumentException("Professor nao associado a disciplina.");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Informe o nome da avaliacao.");
        }
        return store.findAssessmentByName(subjectId, teacherId, nome)
                .orElseGet(() -> store.createAssessment(subjectId, teacherId, nome));
    }

    public List<Assessment> listarAvaliacoesDaDisciplinaDoProfessor(int teacherId, int subjectId) {
        return store.getAssessmentsByTeacherAndSubject(teacherId, subjectId);
    }

    public List<GradeView> listarNotasDaAvaliacao(int assessmentId) {
        return store.getGradesByAssessment(assessmentId)
                .stream()
                .map(g -> new GradeView(
                        g.getId(),
                        g.getAvaliacao(),
                        g.getNota(),
                        store.findSubject(g.getSubjectId()).map(Subject::getNome).orElse("N/A"),
                        store.findTeacher(g.getTeacherId()).map(Teacher::getNome).orElse("N/A"),
                        g.getStudentId(),
                        store.findStudent(g.getStudentId()).map(Student::getNome).orElse("N/A")
                ))
                .collect(Collectors.toList());
    }

    public GradeRecord atualizarNota(int teacherId, int gradeId, double nota) {
        GradeRecord existing = store.findGrade(gradeId)
                .orElseThrow(() -> new IllegalArgumentException("Nota nao encontrada."));
        Teacher teacher = store.findTeacher(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Professor nao encontrado."));
        if (!teacher.getSubjectIds().contains(existing.getSubjectId())) {
            throw new IllegalArgumentException("Professor nao pode editar esta nota.");
        }
        if (nota < 0 || nota > 20) {
            throw new IllegalArgumentException("Nota invalida. Use intervalo de 0 a 20.");
        }
        return store.updateGrade(gradeId, nota);
    }

    public GradeRecord publicarNotaPorAvaliacao(int teacherId, int assessmentId, int studentId, double nota) {
        Assessment assessment = store.findAssessment(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Avaliacao nao encontrada."));
        if (assessment.getTeacherId() != teacherId) {
            throw new IllegalArgumentException("Avaliacao nao pertence ao professor.");
        }
        Student student = store.findStudent(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Estudante nao encontrado."));
        Subject subject = store.findSubject(assessment.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Disciplina nao encontrada."));
        Teacher teacher = store.findTeacher(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Professor nao encontrado."));

        if (!teacher.getSubjectIds().contains(subject.getId())) {
            throw new IllegalArgumentException("Professor nao associado a disciplina.");
        }
        if (!store.isStudentEnrolledInSubject(studentId, subject.getId())) {
            throw new IllegalArgumentException("Estudante nao esta inscrito na disciplina.");
        }
        if (nota < 0 || nota > 20) {
            throw new IllegalArgumentException("Nota invalida. Use intervalo de 0 a 20.");
        }
        Optional<GradeRecord> existing = store.findGradeByAssessmentAndStudent(assessmentId, studentId);
        if (existing.isPresent()) {
            return store.updateGrade(existing.get().getId(), nota);
        }
        return store.createGradeForAssessment(
                assessmentId,
                studentId,
                subject.getId(),
                teacherId,
                assessment.getNome(),
                nota
        );
    }

    public List<GradeView> listarNotasEstudante(int studentId) {
        return store.getGradesByStudent(studentId)
                .stream()
                .filter(g -> store.isStudentEnrolledInSubject(studentId, g.getSubjectId()))
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

    public List<Subject> listarDisciplinasInscritasDoEstudante(int studentId) {
        return store.getEnrolledSubjectsByStudent(studentId);
    }

    public List<Student> listarEstudantesInscritosNaDisciplina(int subjectId) {
        return store.getEnrolledStudentsBySubject(subjectId);
    }

    public Optional<User> buscarUsuario(int id) {
        return store.findUser(id);
    }

    public Optional<Student> findStudent(int id) {
        return store.findStudent(id);
    }

    public Optional<Course> findCourse(int id) {
        return store.findCourse(id);
    }

    public Optional<Teacher> findTeacher(int id) {
        return store.findTeacher(id);
    }

    private void validarCurso(int courseId) {
        if (store.findCourse(courseId).isEmpty()) {
            throw new IllegalArgumentException("Curso nao encontrado.");
        }
    }

    private List<Student> listarEstudantesPorCurso(int courseId) {
        return store.getStudents().stream()
                .filter(s -> s.getCourseId() != null && s.getCourseId() == courseId)
                .collect(Collectors.toList());
    }

    private List<Subject> listarDisciplinasPorCurso(int courseId) {
        return store.getSubjects().stream()
                .filter(s -> s.getCourseId() == courseId)
                .collect(Collectors.toList());
    }

    public static class GradeView {
        private final int id;
        private final String avaliacao;
        private final double nota;
        private final String disciplina;
        private final String professor;
        private final int studentId;
        private final String estudante;

        public GradeView(int id, String avaliacao, double nota, String disciplina, String professor) {
            this(id, avaliacao, nota, disciplina, professor, -1, "");
        }

        public GradeView(int id, String avaliacao, double nota, String disciplina, String professor, int studentId, String estudante) {
            this.id = id;
            this.avaliacao = avaliacao;
            this.nota = nota;
            this.disciplina = disciplina;
            this.professor = professor;
            this.studentId = studentId;
            this.estudante = estudante;
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

        public int getStudentId() {
            return studentId;
        }

        public String getEstudante() {
            return estudante;
        }
    }
}

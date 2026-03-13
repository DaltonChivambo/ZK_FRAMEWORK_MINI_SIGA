package org.example.vm;

import org.example.bootstrap.AppContext;
import org.example.model.*;
import org.example.service.SchoolService;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Messagebox;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DashboardViewModel {
    private final SchoolService schoolService = AppContext.schoolService();

    private User authUser;

    private String studentNome;
    private String studentUsername;
    private String studentPassword;
    private String teacherNome;
    private String teacherUsername;
    private String teacherPassword;
    private String courseNome;
    private String subjectNome;
    private Integer subjectCourseId;

    private Integer selectedStudentId;
    private Integer selectedTeacherId;
    private Integer selectedCourseId;
    private Integer selectedSubjectId;
    private Student selectedStudentForCourse;
    private Course selectedCourseForStudent;
    private Teacher selectedTeacherForSubject;
    private Subject selectedSubjectForTeacher;
    private Student selectedStudentForEnrollment;
    private Subject selectedSubjectForEnrollment;
    private Subject selectedSubjectForGrade;
    private Assessment selectedAssessmentForGrade;
    private Student selectedStudentForGrade;

    private String avaliacao;
    private String newAssessmentName;
    private Double nota;
    private String activeSection;

    @Init
    @NotifyChange("*")
    public void init() {
        Session session = Sessions.getCurrent();
        Integer userId = (Integer) session.getAttribute("authUserId");
        if (userId == null) {
            Executions.sendRedirect("/login.zul");
            return;
        }
        this.authUser = schoolService.buscarUsuario(userId).orElse(null);
        if (authUser == null) {
            Executions.sendRedirect("/login.zul");
            return;
        }
        enforceRolePage();
        definirSecaoInicial();
    }

    @Command
    public void logout() {
        Sessions.getCurrent().invalidate();
        Executions.sendRedirect("/login.zul");
    }

    @Command
    public void goHome() {
        Executions.sendRedirect(homePathByRole());
    }

    @Command
    @NotifyChange("*")
    public void goTo(@BindingParam("section") String section) {
        if (section == null || section.isBlank()) {
            return;
        }
        if (!allowedSection(section)) {
            throw new IllegalArgumentException("Secao indisponivel para o seu perfil.");
        }
        this.activeSection = section;
    }

    @Command
    @NotifyChange("*")
    public void criarEstudante() {
        if (studentNome == null || studentNome.isBlank() ||
                studentUsername == null || studentUsername.isBlank() ||
                studentPassword == null || studentPassword.isBlank()) {
            throw new IllegalArgumentException("Preencha nome, username e password do estudante.");
        }
        schoolService.registrarEstudante(studentNome, studentUsername, studentPassword);
        studentNome = "";
        studentUsername = "";
        studentPassword = "";
        Messagebox.show("Estudante criado com sucesso.");
    }

    @Command
    @NotifyChange("*")
    public void criarProfessor() {
        if (teacherNome == null || teacherNome.isBlank() ||
                teacherUsername == null || teacherUsername.isBlank() ||
                teacherPassword == null || teacherPassword.isBlank()) {
            throw new IllegalArgumentException("Preencha nome, username e password do professor.");
        }
        schoolService.registrarProfessor(teacherNome, teacherUsername, teacherPassword);
        teacherNome = "";
        teacherUsername = "";
        teacherPassword = "";
        Messagebox.show("Professor criado com sucesso.");
    }

    @Command
    @NotifyChange("*")
    public void criarCurso() {
        if (courseNome == null || courseNome.isBlank()) {
            throw new IllegalArgumentException("Informe o nome do curso.");
        }
        schoolService.registrarCurso(courseNome);
        courseNome = "";
        Messagebox.show("Curso criado com sucesso.");
    }

    @Command
    @NotifyChange("*")
    public void criarDisciplina() {
        if (subjectNome == null || subjectNome.isBlank() || subjectCourseId == null) {
            throw new IllegalArgumentException("Informe disciplina e curso.");
        }
        schoolService.registrarDisciplina(subjectNome, subjectCourseId);
        subjectNome = "";
        subjectCourseId = null;
        Messagebox.show("Disciplina criada com sucesso.");
    }

    @Command
    @NotifyChange("*")
    public void associarEstudanteCurso() {
        if (selectedStudentForCourse == null || selectedCourseForStudent == null) {
            throw new IllegalArgumentException("Selecione estudante e curso.");
        }
        schoolService.associarEstudanteAoCurso(selectedStudentForCourse.getId(), selectedCourseForStudent.getId());
        Messagebox.show("Estudante associado ao curso.");
    }

    @Command
    @NotifyChange("*")
    public void associarProfessorDisciplina() {
        if (selectedTeacherForSubject == null || selectedSubjectForTeacher == null) {
            throw new IllegalArgumentException("Selecione professor e disciplina.");
        }
        schoolService.associarProfessorADisciplina(selectedTeacherForSubject.getId(), selectedSubjectForTeacher.getId());
        Messagebox.show("Professor associado a disciplina.");
    }

    @Command
    @NotifyChange("*")
    public void associarEstudanteDisciplina() {
        if (selectedStudentForEnrollment == null || selectedSubjectForEnrollment == null) {
            throw new IllegalArgumentException("Selecione estudante e disciplina.");
        }
        schoolService.associarEstudanteADisciplina(selectedStudentForEnrollment.getId(), selectedSubjectForEnrollment.getId());
        Messagebox.show("Estudante inscrito na disciplina.");
    }

    @Command
    @NotifyChange({"selectedAssessmentForGrade", "subjectAssessmentsForGrade", "assessmentGradesForEditor", "newAssessmentName"})
    public void criarAvaliacaoProfessor() {
        if (selectedSubjectForGrade == null || newAssessmentName == null || newAssessmentName.isBlank()) {
            throw new IllegalArgumentException("Selecione disciplina e informe o nome da avaliacao.");
        }
        selectedAssessmentForGrade = schoolService.criarAvaliacao(
                authUser.getId(),
                selectedSubjectForGrade.getId(),
                newAssessmentName
        );
        newAssessmentName = "";
        Messagebox.show("Avaliacao criada com sucesso.");
    }

    @Command
    @NotifyChange({"nota", "assessmentGradesForEditor"})
    public void publicarNota() {
        if (selectedAssessmentForGrade == null || selectedStudentForGrade == null || nota == null) {
            throw new IllegalArgumentException("Selecione avaliacao, estudante e nota.");
        }
        schoolService.publicarNotaPorAvaliacao(
                authUser.getId(),
                selectedAssessmentForGrade.getId(),
                selectedStudentForGrade.getId(),
                nota
        );
        nota = null;
        Messagebox.show("Nota publicada com sucesso.");
    }

    @Command
    @NotifyChange({
            "selectedStudentForGrade",
            "selectedAssessmentForGrade",
            "eligibleStudentsForGrade",
            "selectedSubjectCourseName",
            "subjectAssessmentsForGrade",
            "assessmentGradesForEditor"
    })
    public void onTeacherSubjectChange() {
        selectedStudentForGrade = null;
        selectedAssessmentForGrade = null;
    }

    @Command
    @NotifyChange({"selectedStudentForGrade", "eligibleStudentsForGrade", "assessmentGradesForEditor"})
    public void onTeacherAssessmentChange() {
        selectedStudentForGrade = null;
    }

    @Command
    @NotifyChange("assessmentGradesForEditor")
    public void atualizarNota(@BindingParam("row") AssessmentGradeEditorView row) {
        if (row == null) {
            throw new IllegalArgumentException("Linha de nota invalida.");
        }
        schoolService.atualizarNota(authUser.getId(), row.getGradeId(), row.getNota());
        Messagebox.show("Nota atualizada com sucesso.");
    }

    @Command
    @NotifyChange({"selectedSubjectForEnrollment", "eligibleSubjectsForEnrollment"})
    public void onEnrollmentStudentChange() {
        selectedSubjectForEnrollment = null;
    }

    private void definirSecaoInicial() {
        String path = Executions.getCurrent().getDesktop().getRequestPath();
        if (path.endsWith("/admin.zul")) {
            this.activeSection = "admin-students";
            return;
        }
        if (path.endsWith("/teacher.zul")) {
            this.activeSection = "teacher-grades";
            return;
        }
        if (path.endsWith("/student.zul")) {
            this.activeSection = "student-grades";
            return;
        }
        if (isAdmin()) {
            this.activeSection = "admin-students";
            return;
        }
        if (isProfessor()) {
            this.activeSection = "teacher-grades";
            return;
        }
        this.activeSection = "student-grades";
    }

    private boolean allowedSection(String section) {
        if (isAdmin()) {
            return section.startsWith("admin-");
        }
        if (isProfessor()) {
            return section.equals("teacher-grades") || section.equals("teacher-ids");
        }
        return section.equals("student-grades");
    }

    private void enforceRolePage() {
        String path = Executions.getCurrent().getDesktop().getRequestPath();
        if (path.endsWith("/admin.zul") && !isAdmin()) {
            Executions.sendRedirect(homePathByRole());
            return;
        }
        if (path.endsWith("/teacher.zul") && !isProfessor()) {
            Executions.sendRedirect(homePathByRole());
            return;
        }
        if (path.endsWith("/student.zul") && !isEstudante()) {
            Executions.sendRedirect(homePathByRole());
        }
    }

    private String homePathByRole() {
        if (isAdmin()) {
            return "/admin.zul";
        }
        if (isProfessor()) {
            return "/teacher.zul";
        }
        return "/student.zul";
    }

    public String getDisplayName() {
        return authUser == null ? "" : authUser.getNome();
    }

    public String getRoleName() {
        return authUser == null ? "" : authUser.getRole().name();
    }

    public boolean isAdmin() {
        return authUser != null && authUser.getRole() == Role.ADMIN;
    }

    public boolean isProfessor() {
        return authUser != null && authUser.getRole() == Role.PROFESSOR;
    }

    public boolean isEstudante() {
        return authUser != null && authUser.getRole() == Role.ESTUDANTE;
    }

    public List<Student> getStudents() {
        return schoolService.listarEstudantes();
    }

    public List<Teacher> getTeachers() {
        return schoolService.listarProfessores();
    }

    public List<Course> getCourses() {
        return schoolService.listarCursos();
    }

    public List<Subject> getSubjects() {
        return schoolService.listarDisciplinas();
    }

    public List<SchoolService.GradeView> getMyGrades() {
        if (!isEstudante()) {
            return List.of();
        }
        return schoolService.listarNotasEstudante(authUser.getId());
    }

    public List<Subject> getMySubjects() {
        if (!isProfessor()) {
            return List.of();
        }
        return schoolService.listarDisciplinas().stream()
                .filter(s -> s.getTeacherId() != null && s.getTeacherId() == authUser.getId())
                .collect(Collectors.toList());
    }

    public List<Student> getEligibleStudentsForGrade() {
        if (!isProfessor()) {
            return List.of();
        }
        if (selectedAssessmentForGrade != null) {
            return schoolService.listarEstudantesInscritosNaDisciplina(selectedAssessmentForGrade.getSubjectId());
        }
        if (selectedSubjectForGrade != null) {
            return schoolService.listarEstudantesInscritosNaDisciplina(selectedSubjectForGrade.getId());
        }
        return List.of();
    }

    public List<Assessment> getSubjectAssessmentsForGrade() {
        if (!isProfessor() || selectedSubjectForGrade == null) {
            return List.of();
        }
        return schoolService.listarAvaliacoesDaDisciplinaDoProfessor(authUser.getId(), selectedSubjectForGrade.getId());
    }

    public List<AssessmentGradeEditorView> getAssessmentGradesForEditor() {
        if (!isProfessor() || selectedAssessmentForGrade == null) {
            return List.of();
        }
        return schoolService.listarNotasDaAvaliacao(selectedAssessmentForGrade.getId()).stream()
                .map(g -> new AssessmentGradeEditorView(
                        g.getId(),
                        g.getStudentId(),
                        g.getEstudante(),
                        g.getNota()
                ))
                .collect(Collectors.toList());
    }

    public List<Subject> getEligibleSubjectsForEnrollment() {
        if (selectedStudentForEnrollment == null || selectedStudentForEnrollment.getCourseId() == null) {
            return List.of();
        }
        int courseId = selectedStudentForEnrollment.getCourseId();
        return schoolService.listarDisciplinas().stream()
                .filter(s -> s.getCourseId() == courseId)
                .collect(Collectors.toList());
    }

    public int getEligibleStudentCount() {
        if (!isProfessor()) {
            return 0;
        }
        Set<Integer> studentIds = getMySubjects().stream()
                .flatMap(subject -> schoolService.listarEstudantesInscritosNaDisciplina(subject.getId()).stream())
                .map(Student::getId)
                .collect(Collectors.toSet());
        return studentIds.size();
    }

    public String getSelectedSubjectCourseName() {
        if (selectedSubjectForGrade == null) {
            return "Selecione uma disciplina";
        }
        return schoolService.listarCursos().stream()
                .filter(c -> c.getId() == selectedSubjectForGrade.getCourseId())
                .map(Course::getNome)
                .findFirst()
                .orElse("Curso nao encontrado");
    }

    public int getStudentCount() {
        return schoolService.listarEstudantes().size();
    }

    public int getTeacherCount() {
        return schoolService.listarProfessores().size();
    }

    public int getCourseCount() {
        return schoolService.listarCursos().size();
    }

    public int getSubjectCount() {
        return schoolService.listarDisciplinas().size();
    }

    public String getMyAverage() {
        if (!isEstudante()) {
            return "-";
        }
        List<SchoolService.GradeView> grades = getMyGrades();
        if (grades.isEmpty()) {
            return "Sem notas";
        }
        double avg = grades.stream().mapToDouble(SchoolService.GradeView::getNota).average().orElse(0);
        return String.format("%.2f", avg);
    }

    public List<SubjectAverageView> getMySubjectAverages() {
        if (!isEstudante()) {
            return List.of();
        }
        return getMyGrades().stream()
                .collect(Collectors.groupingBy(SchoolService.GradeView::getDisciplina))
                .entrySet()
                .stream()
                .map(entry -> {
                    double avg = entry.getValue().stream()
                            .mapToDouble(SchoolService.GradeView::getNota)
                            .average()
                            .orElse(0);
                    return new SubjectAverageView(
                            entry.getKey(),
                            String.format("%.2f", avg),
                            entry.getValue().size()
                    );
                })
                .sorted(Comparator.comparing(SubjectAverageView::getDisciplina))
                .collect(Collectors.toList());
    }

    public String getMyCourseName() {
        if (!isEstudante()) {
            return "-";
        }
        Student me = schoolService.findStudent(authUser.getId()).orElse(null);
        if (me == null || me.getCourseId() == null) {
            return "Nao associado";
        }
        return schoolService.findCourse(me.getCourseId())
                .map(Course::getNome)
                .orElse("Nao associado");
    }

    public List<Subject> getMyCourseSubjects() {
        if (!isEstudante()) {
            return List.of();
        }
        return schoolService.listarDisciplinasInscritasDoEstudante(authUser.getId());
    }

    public List<StudentEnrolledSubjectView> getMyCourseSubjectViews() {
        if (!isEstudante()) {
            return List.of();
        }
        return schoolService.listarDisciplinasInscritasDoEstudante(authUser.getId()).stream()
                .map(subject -> new StudentEnrolledSubjectView(
                        subject.getId(),
                        subject.getNome(),
                        getTeacherName(subject.getTeacherId())
                ))
                .collect(Collectors.toList());
    }

    public String getTeacherName(Integer teacherId) {
        if (teacherId == null) {
            return "Sem professor";
        }
        return schoolService.findTeacher(teacherId)
                .map(Teacher::getNome)
                .orElse("Sem professor");
    }

    public List<StudentCourseAssociationView> getStudentCourseAssociations() {
        Map<Integer, String> courseById = schoolService.listarCursos().stream()
                .collect(Collectors.toMap(Course::getId, Course::getNome, (a, b) -> a));
        return schoolService.listarEstudantes().stream()
                .filter(s -> s.getCourseId() != null)
                .map(s -> new StudentCourseAssociationView(
                        s.getId(),
                        s.getNome(),
                        s.getCourseId(),
                        courseById.getOrDefault(s.getCourseId(), "Curso nao encontrado"),
                        schoolService.listarDisciplinasInscritasDoEstudante(s.getId()).size()
                ))
                .collect(Collectors.toList());
    }

    public List<TeacherSubjectAssociationView> getTeacherSubjectAssociations() {
        Map<Integer, String> teacherById = schoolService.listarProfessores().stream()
                .collect(Collectors.toMap(Teacher::getId, Teacher::getNome, (a, b) -> a));
        Map<Integer, String> courseById = schoolService.listarCursos().stream()
                .collect(Collectors.toMap(Course::getId, Course::getNome, (a, b) -> a));
        return schoolService.listarDisciplinas().stream()
                .filter(s -> s.getTeacherId() != null)
                .map(s -> new TeacherSubjectAssociationView(
                        s.getTeacherId(),
                        teacherById.getOrDefault(s.getTeacherId(), "Professor nao encontrado"),
                        s.getId(),
                        s.getNome(),
                        s.getCourseId(),
                        courseById.getOrDefault(s.getCourseId(), "Curso nao encontrado"),
                        schoolService.listarEstudantesInscritosNaDisciplina(s.getId()).size()
                ))
                .collect(Collectors.toList());
    }

    public List<StudentSubjectAssociationView> getStudentSubjectAssociations() {
        Map<Integer, String> courseById = schoolService.listarCursos().stream()
                .collect(Collectors.toMap(Course::getId, Course::getNome, (a, b) -> a));
        return schoolService.listarEstudantes().stream()
                .flatMap(student -> schoolService.listarDisciplinasInscritasDoEstudante(student.getId()).stream()
                        .map(subject -> new StudentSubjectAssociationView(
                                student.getId(),
                                student.getNome(),
                                subject.getId(),
                                subject.getNome(),
                                subject.getCourseId(),
                                courseById.getOrDefault(subject.getCourseId(), "Curso nao encontrado")
                        )))
                .collect(Collectors.toList());
    }

    public String getActiveSection() {
        return activeSection;
    }

    public String getPageTitle() {
        if (activeSection == null) {
            return "Dashboard";
        }
        switch (activeSection) {
            case "admin-users":
                return "Gestao de Utilizadores";
            case "admin-students":
                return "Cadastro de Estudantes";
            case "admin-teachers":
                return "Cadastro de Professores";
            case "admin-courses":
                return "Cadastro de Cursos";
            case "admin-subjects":
                return "Cadastro de Disciplinas";
            case "admin-assignments":
                return "Associacoes";
            case "admin-ids":
                return "Referencias de IDs";
            case "teacher-grades":
                return "Publicar Notas";
            case "teacher-ids":
                return "Referencias de IDs";
            case "student-grades":
                return "Minhas Notas";
            default:
                return "Dashboard";
        }
    }

    public String getStudentNome() {
        return studentNome;
    }

    public void setStudentNome(String studentNome) {
        this.studentNome = studentNome;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public void setStudentUsername(String studentUsername) {
        this.studentUsername = studentUsername;
    }

    public String getStudentPassword() {
        return studentPassword;
    }

    public void setStudentPassword(String studentPassword) {
        this.studentPassword = studentPassword;
    }

    public String getTeacherNome() {
        return teacherNome;
    }

    public void setTeacherNome(String teacherNome) {
        this.teacherNome = teacherNome;
    }

    public String getTeacherUsername() {
        return teacherUsername;
    }

    public void setTeacherUsername(String teacherUsername) {
        this.teacherUsername = teacherUsername;
    }

    public String getTeacherPassword() {
        return teacherPassword;
    }

    public void setTeacherPassword(String teacherPassword) {
        this.teacherPassword = teacherPassword;
    }

    public String getCourseNome() {
        return courseNome;
    }

    public void setCourseNome(String courseNome) {
        this.courseNome = courseNome;
    }

    public String getSubjectNome() {
        return subjectNome;
    }

    public void setSubjectNome(String subjectNome) {
        this.subjectNome = subjectNome;
    }

    public Integer getSubjectCourseId() {
        return subjectCourseId;
    }

    public void setSubjectCourseId(Integer subjectCourseId) {
        this.subjectCourseId = subjectCourseId;
    }

    public Integer getSelectedStudentId() {
        return selectedStudentId;
    }

    public void setSelectedStudentId(Integer selectedStudentId) {
        this.selectedStudentId = selectedStudentId;
    }

    public Integer getSelectedTeacherId() {
        return selectedTeacherId;
    }

    public void setSelectedTeacherId(Integer selectedTeacherId) {
        this.selectedTeacherId = selectedTeacherId;
    }

    public Student getSelectedStudentForCourse() {
        return selectedStudentForCourse;
    }

    public void setSelectedStudentForCourse(Student selectedStudentForCourse) {
        this.selectedStudentForCourse = selectedStudentForCourse;
    }

    public Course getSelectedCourseForStudent() {
        return selectedCourseForStudent;
    }

    public void setSelectedCourseForStudent(Course selectedCourseForStudent) {
        this.selectedCourseForStudent = selectedCourseForStudent;
    }

    public Teacher getSelectedTeacherForSubject() {
        return selectedTeacherForSubject;
    }

    public void setSelectedTeacherForSubject(Teacher selectedTeacherForSubject) {
        this.selectedTeacherForSubject = selectedTeacherForSubject;
    }

    public Subject getSelectedSubjectForTeacher() {
        return selectedSubjectForTeacher;
    }

    public void setSelectedSubjectForTeacher(Subject selectedSubjectForTeacher) {
        this.selectedSubjectForTeacher = selectedSubjectForTeacher;
    }

    public Integer getSelectedCourseId() {
        return selectedCourseId;
    }

    public void setSelectedCourseId(Integer selectedCourseId) {
        this.selectedCourseId = selectedCourseId;
    }

    public Integer getSelectedSubjectId() {
        return selectedSubjectId;
    }

    public void setSelectedSubjectId(Integer selectedSubjectId) {
        this.selectedSubjectId = selectedSubjectId;
    }

    public Subject getSelectedSubjectForGrade() {
        return selectedSubjectForGrade;
    }

    public void setSelectedSubjectForGrade(Subject selectedSubjectForGrade) {
        this.selectedSubjectForGrade = selectedSubjectForGrade;
    }

    public Student getSelectedStudentForGrade() {
        return selectedStudentForGrade;
    }

    public void setSelectedStudentForGrade(Student selectedStudentForGrade) {
        this.selectedStudentForGrade = selectedStudentForGrade;
    }

    public Student getSelectedStudentForEnrollment() {
        return selectedStudentForEnrollment;
    }

    public void setSelectedStudentForEnrollment(Student selectedStudentForEnrollment) {
        this.selectedStudentForEnrollment = selectedStudentForEnrollment;
    }

    public Subject getSelectedSubjectForEnrollment() {
        return selectedSubjectForEnrollment;
    }

    public void setSelectedSubjectForEnrollment(Subject selectedSubjectForEnrollment) {
        this.selectedSubjectForEnrollment = selectedSubjectForEnrollment;
    }

    public String getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(String avaliacao) {
        this.avaliacao = avaliacao;
    }

    public String getNewAssessmentName() {
        return newAssessmentName;
    }

    public void setNewAssessmentName(String newAssessmentName) {
        this.newAssessmentName = newAssessmentName;
    }

    public Double getNota() {
        return nota;
    }

    public void setNota(Double nota) {
        this.nota = nota;
    }

    public Assessment getSelectedAssessmentForGrade() {
        return selectedAssessmentForGrade;
    }

    public void setSelectedAssessmentForGrade(Assessment selectedAssessmentForGrade) {
        this.selectedAssessmentForGrade = selectedAssessmentForGrade;
    }

    public static class StudentCourseAssociationView {
        private final int studentId;
        private final String studentName;
        private final int courseId;
        private final String courseName;
        private final int enrolledSubjects;

        public StudentCourseAssociationView(int studentId, String studentName, int courseId, String courseName, int enrolledSubjects) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.courseId = courseId;
            this.courseName = courseName;
            this.enrolledSubjects = enrolledSubjects;
        }

        public int getStudentId() {
            return studentId;
        }

        public String getStudentName() {
            return studentName;
        }

        public int getCourseId() {
            return courseId;
        }

        public String getCourseName() {
            return courseName;
        }

        public int getEnrolledSubjects() {
            return enrolledSubjects;
        }
    }

    public static class AssessmentGradeEditorView {
        private final int gradeId;
        private final int studentId;
        private final String studentName;
        private double nota;

        public AssessmentGradeEditorView(int gradeId, int studentId, String studentName, double nota) {
            this.gradeId = gradeId;
            this.studentId = studentId;
            this.studentName = studentName;
            this.nota = nota;
        }

        public int getGradeId() {
            return gradeId;
        }

        public int getStudentId() {
            return studentId;
        }

        public String getStudentName() {
            return studentName;
        }

        public double getNota() {
            return nota;
        }

        public void setNota(double nota) {
            this.nota = nota;
        }
    }

    public static class TeacherSubjectAssociationView {
        private final int teacherId;
        private final String teacherName;
        private final int subjectId;
        private final String subjectName;
        private final int courseId;
        private final String courseName;
        private final int enrolledStudents;

        public TeacherSubjectAssociationView(int teacherId, String teacherName, int subjectId, String subjectName, int courseId, String courseName, int enrolledStudents) {
            this.teacherId = teacherId;
            this.teacherName = teacherName;
            this.subjectId = subjectId;
            this.subjectName = subjectName;
            this.courseId = courseId;
            this.courseName = courseName;
            this.enrolledStudents = enrolledStudents;
        }

        public int getTeacherId() {
            return teacherId;
        }

        public String getTeacherName() {
            return teacherName;
        }

        public int getSubjectId() {
            return subjectId;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public int getCourseId() {
            return courseId;
        }

        public String getCourseName() {
            return courseName;
        }

        public int getEnrolledStudents() {
            return enrolledStudents;
        }
    }

    public static class StudentSubjectAssociationView {
        private final int studentId;
        private final String studentName;
        private final int subjectId;
        private final String subjectName;
        private final int courseId;
        private final String courseName;

        public StudentSubjectAssociationView(int studentId, String studentName, int subjectId, String subjectName, int courseId, String courseName) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.subjectId = subjectId;
            this.subjectName = subjectName;
            this.courseId = courseId;
            this.courseName = courseName;
        }

        public int getStudentId() {
            return studentId;
        }

        public String getStudentName() {
            return studentName;
        }

        public int getSubjectId() {
            return subjectId;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public int getCourseId() {
            return courseId;
        }

        public String getCourseName() {
            return courseName;
        }
    }

    public static class StudentEnrolledSubjectView {
        private final int id;
        private final String nome;
        private final String teacherName;

        public StudentEnrolledSubjectView(int id, String nome, String teacherName) {
            this.id = id;
            this.nome = nome;
            this.teacherName = teacherName;
        }

        public int getId() {
            return id;
        }

        public String getNome() {
            return nome;
        }

        public String getTeacherName() {
            return teacherName;
        }
    }

    public static class SubjectAverageView {
        private final String disciplina;
        private final String media;
        private final int totalAvaliacoes;

        public SubjectAverageView(String disciplina, String media, int totalAvaliacoes) {
            this.disciplina = disciplina;
            this.media = media;
            this.totalAvaliacoes = totalAvaliacoes;
        }

        public String getDisciplina() {
            return disciplina;
        }

        public String getMedia() {
            return media;
        }

        public int getTotalAvaliacoes() {
            return totalAvaliacoes;
        }
    }
}

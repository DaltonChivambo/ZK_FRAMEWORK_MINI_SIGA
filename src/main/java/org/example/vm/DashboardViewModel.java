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

import java.util.List;
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

    private String avaliacao;
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
    public void publicarNota() {
        if (selectedStudentId == null || selectedSubjectId == null || avaliacao == null || avaliacao.isBlank() || nota == null) {
            throw new IllegalArgumentException("Preencha os dados da avaliacao.");
        }
        schoolService.publicarNota(authUser.getId(), selectedStudentId, selectedSubjectId, avaliacao, nota);
        avaliacao = "";
        nota = null;
        Messagebox.show("Nota publicada com sucesso.");
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

    public String getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(String avaliacao) {
        this.avaliacao = avaliacao;
    }

    public Double getNota() {
        return nota;
    }

    public void setNota(Double nota) {
        this.nota = nota;
    }
}

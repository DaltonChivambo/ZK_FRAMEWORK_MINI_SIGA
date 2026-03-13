package org.example.store;

import org.example.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class InMemorySchoolStore implements SchoolStore {
    private final AtomicInteger userSeq = new AtomicInteger(1);
    private final AtomicInteger courseSeq = new AtomicInteger(1);
    private final AtomicInteger subjectSeq = new AtomicInteger(1);
    private final AtomicInteger gradeSeq = new AtomicInteger(1);

    private final Map<Integer, User> users = new ConcurrentHashMap<>();
    private final Map<Integer, Course> courses = new ConcurrentHashMap<>();
    private final Map<Integer, Subject> subjects = new ConcurrentHashMap<>();
    private final Map<Integer, GradeRecord> grades = new ConcurrentHashMap<>();

    @Override
    public Admin createAdmin(String nome, String username, String password) {
        Admin admin = new Admin(userSeq.getAndIncrement(), nome, username, password);
        users.put(admin.getId(), admin);
        return admin;
    }

    @Override
    public Student createStudent(String nome, String username, String password) {
        Student student = new Student(userSeq.getAndIncrement(), nome, username, password);
        users.put(student.getId(), student);
        return student;
    }

    @Override
    public Teacher createTeacher(String nome, String username, String password) {
        Teacher teacher = new Teacher(userSeq.getAndIncrement(), nome, username, password);
        users.put(teacher.getId(), teacher);
        return teacher;
    }

    @Override
    public Course createCourse(String nome) {
        Course course = new Course(courseSeq.getAndIncrement(), nome);
        courses.put(course.getId(), course);
        return course;
    }

    @Override
    public Subject createSubject(String nome, int courseId) {
        Subject subject = new Subject(subjectSeq.getAndIncrement(), nome, courseId);
        subjects.put(subject.getId(), subject);
        return subject;
    }

    @Override
    public GradeRecord createGrade(int studentId, int subjectId, int teacherId, String avaliacao, double nota) {
        GradeRecord grade = new GradeRecord(
                gradeSeq.getAndIncrement(),
                studentId,
                subjectId,
                teacherId,
                avaliacao,
                nota
        );
        grades.put(grade.getId(), grade);
        return grade;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return users.values()
                .stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    @Override
    public Optional<User> findUser(int userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Optional<Student> findStudent(int studentId) {
        User user = users.get(studentId);
        return user instanceof Student ? Optional.of((Student) user) : Optional.empty();
    }

    @Override
    public Optional<Teacher> findTeacher(int teacherId) {
        User user = users.get(teacherId);
        return user instanceof Teacher ? Optional.of((Teacher) user) : Optional.empty();
    }

    @Override
    public Optional<Course> findCourse(int courseId) {
        return Optional.ofNullable(courses.get(courseId));
    }

    @Override
    public Optional<Subject> findSubject(int subjectId) {
        return Optional.ofNullable(subjects.get(subjectId));
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public List<Student> getStudents() {
        return users.values()
                .stream()
                .filter(Student.class::isInstance)
                .map(Student.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<Teacher> getTeachers() {
        return users.values()
                .stream()
                .filter(Teacher.class::isInstance)
                .map(Teacher.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<Course> getCourses() {
        return new ArrayList<>(courses.values());
    }

    @Override
    public List<Subject> getSubjects() {
        return new ArrayList<>(subjects.values());
    }

    @Override
    public List<GradeRecord> getGradesByStudent(int studentId) {
        return grades.values()
                .stream()
                .filter(g -> g.getStudentId() == studentId)
                .collect(Collectors.toList());
    }

    @Override
    public void assignStudentToCourse(int studentId, int courseId) {
        Student student = findStudent(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Estudante nao encontrado."));
        student.setCourseId(courseId);
    }

    @Override
    public void assignTeacherToSubject(int teacherId, int subjectId) {
        Teacher teacher = findTeacher(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Professor nao encontrado."));
        Subject subject = findSubject(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Disciplina nao encontrada."));
        subject.setTeacherId(teacherId);
        teacher.getSubjectIds().add(subjectId);
    }

    @Override
    public boolean hasAnyUsers() {
        return !users.isEmpty();
    }
}

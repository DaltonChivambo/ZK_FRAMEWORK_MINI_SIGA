package org.example.store;

import org.example.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class InMemorySchoolStore {
    private final AtomicInteger userSeq = new AtomicInteger(1);
    private final AtomicInteger courseSeq = new AtomicInteger(1);
    private final AtomicInteger subjectSeq = new AtomicInteger(1);
    private final AtomicInteger gradeSeq = new AtomicInteger(1);

    private final Map<Integer, User> users = new ConcurrentHashMap<>();
    private final Map<Integer, Course> courses = new ConcurrentHashMap<>();
    private final Map<Integer, Subject> subjects = new ConcurrentHashMap<>();
    private final Map<Integer, GradeRecord> grades = new ConcurrentHashMap<>();

    public Admin createAdmin(String nome, String username, String password) {
        Admin admin = new Admin(userSeq.getAndIncrement(), nome, username, password);
        users.put(admin.getId(), admin);
        return admin;
    }

    public Student createStudent(String nome, String username, String password) {
        Student student = new Student(userSeq.getAndIncrement(), nome, username, password);
        users.put(student.getId(), student);
        return student;
    }

    public Teacher createTeacher(String nome, String username, String password) {
        Teacher teacher = new Teacher(userSeq.getAndIncrement(), nome, username, password);
        users.put(teacher.getId(), teacher);
        return teacher;
    }

    public Course createCourse(String nome) {
        Course course = new Course(courseSeq.getAndIncrement(), nome);
        courses.put(course.getId(), course);
        return course;
    }

    public Subject createSubject(String nome, int courseId) {
        Subject subject = new Subject(subjectSeq.getAndIncrement(), nome, courseId);
        subjects.put(subject.getId(), subject);
        return subject;
    }

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

    public Optional<User> findByUsername(String username) {
        return users.values()
                .stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public Optional<User> findUser(int userId) {
        return Optional.ofNullable(users.get(userId));
    }

    public Optional<Student> findStudent(int studentId) {
        User user = users.get(studentId);
        return user instanceof Student ? Optional.of((Student) user) : Optional.empty();
    }

    public Optional<Teacher> findTeacher(int teacherId) {
        User user = users.get(teacherId);
        return user instanceof Teacher ? Optional.of((Teacher) user) : Optional.empty();
    }

    public Optional<Course> findCourse(int courseId) {
        return Optional.ofNullable(courses.get(courseId));
    }

    public Optional<Subject> findSubject(int subjectId) {
        return Optional.ofNullable(subjects.get(subjectId));
    }

    public Collection<User> getUsers() {
        return users.values();
    }

    public List<Student> getStudents() {
        return users.values()
                .stream()
                .filter(Student.class::isInstance)
                .map(Student.class::cast)
                .collect(Collectors.toList());
    }

    public List<Teacher> getTeachers() {
        return users.values()
                .stream()
                .filter(Teacher.class::isInstance)
                .map(Teacher.class::cast)
                .collect(Collectors.toList());
    }

    public List<Course> getCourses() {
        return new ArrayList<>(courses.values());
    }

    public List<Subject> getSubjects() {
        return new ArrayList<>(subjects.values());
    }

    public List<GradeRecord> getGradesByStudent(int studentId) {
        return grades.values()
                .stream()
                .filter(g -> g.getStudentId() == studentId)
                .collect(Collectors.toList());
    }
}

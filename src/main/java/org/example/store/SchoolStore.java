package org.example.store;

import org.example.model.*;

import java.util.List;
import java.util.Optional;

public interface SchoolStore {
    Admin createAdmin(String nome, String username, String password);

    Student createStudent(String nome, String username, String password);

    Teacher createTeacher(String nome, String username, String password);

    Course createCourse(String nome);

    Subject createSubject(String nome, int courseId);

    Assessment createAssessment(int subjectId, int teacherId, String nome);

    GradeRecord createGrade(int studentId, int subjectId, int teacherId, String avaliacao, double nota);

    GradeRecord createGradeForAssessment(int assessmentId, int studentId, int subjectId, int teacherId, String avaliacao, double nota);

    GradeRecord updateGrade(int gradeId, double nota);

    Optional<User> findByUsername(String username);

    Optional<User> findUser(int userId);

    Optional<Student> findStudent(int studentId);

    Optional<Teacher> findTeacher(int teacherId);

    Optional<Course> findCourse(int courseId);

    Optional<Subject> findSubject(int subjectId);

    Optional<Assessment> findAssessment(int assessmentId);

    Optional<Assessment> findAssessmentByName(int subjectId, int teacherId, String nome);

    Optional<GradeRecord> findGrade(int gradeId);

    Optional<GradeRecord> findGradeByAssessmentAndStudent(int assessmentId, int studentId);

    List<User> getUsers();

    List<Student> getStudents();

    List<Teacher> getTeachers();

    List<Course> getCourses();

    List<Subject> getSubjects();

    List<Assessment> getAssessmentsByTeacherAndSubject(int teacherId, int subjectId);

    List<Subject> getEnrolledSubjectsByStudent(int studentId);

    List<GradeRecord> getGradesByStudent(int studentId);

    List<GradeRecord> getGradesByAssessment(int assessmentId);

    void assignStudentToCourse(int studentId, int courseId);

    void assignTeacherToSubject(int teacherId, int subjectId);

    void enrollStudentInSubject(int studentId, int subjectId);

    void clearStudentEnrollments(int studentId);

    boolean isStudentEnrolledInSubject(int studentId, int subjectId);

    List<Student> getEnrolledStudentsBySubject(int subjectId);

    boolean hasAnyUsers();
}

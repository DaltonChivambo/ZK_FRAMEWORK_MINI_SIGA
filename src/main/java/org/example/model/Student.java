package org.example.model;

public class Student extends User {
    private Integer courseId;

    public Student(int id, String nome, String username, String password) {
        super(id, nome, username, password, Role.ESTUDANTE);
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }
}

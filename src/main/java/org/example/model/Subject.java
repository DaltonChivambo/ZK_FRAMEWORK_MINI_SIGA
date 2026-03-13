package org.example.model;

public class Subject {
    private final int id;
    private final String nome;
    private final int courseId;
    private Integer teacherId;

    public Subject(int id, String nome, int courseId) {
        this.id = id;
        this.nome = nome;
        this.courseId = courseId;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getCourseId() {
        return courseId;
    }

    public Integer getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Integer teacherId) {
        this.teacherId = teacherId;
    }
}

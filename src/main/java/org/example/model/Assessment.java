package org.example.model;

public class Assessment {
    private final int id;
    private final int subjectId;
    private final int teacherId;
    private final String nome;

    public Assessment(int id, int subjectId, int teacherId, String nome) {
        this.id = id;
        this.subjectId = subjectId;
        this.teacherId = teacherId;
        this.nome = nome;
    }

    public int getId() {
        return id;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public String getNome() {
        return nome;
    }
}

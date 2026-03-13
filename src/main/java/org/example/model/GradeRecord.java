package org.example.model;

public class GradeRecord {
    private final int id;
    private final int studentId;
    private final int subjectId;
    private final int teacherId;
    private final String avaliacao;
    private final double nota;

    public GradeRecord(int id, int studentId, int subjectId, int teacherId, String avaliacao, double nota) {
        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.teacherId = teacherId;
        this.avaliacao = avaliacao;
        this.nota = nota;
    }

    public int getId() {
        return id;
    }

    public int getStudentId() {
        return studentId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public String getAvaliacao() {
        return avaliacao;
    }

    public double getNota() {
        return nota;
    }
}

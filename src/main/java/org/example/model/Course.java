package org.example.model;

public class Course {
    private final int id;
    private final String nome;

    public Course(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}

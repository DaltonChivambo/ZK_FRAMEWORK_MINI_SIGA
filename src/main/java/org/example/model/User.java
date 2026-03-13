package org.example.model;

public abstract class User {
    private final int id;
    private final String nome;
    private final String username;
    private String password;
    private final Role role;

    protected User(int id, String nome, String username, String password, Role role) {
        this.id = id;
        this.nome = nome;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }
}

package org.example.dto;

import org.example.model.Role;

public class AuthResponse {
    private String token;
    private int userId;
    private String nome;
    private Role role;

    public AuthResponse() {
    }

    public AuthResponse(String token, int userId, String nome, Role role) {
        this.token = token;
        this.userId = userId;
        this.nome = nome;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public int getUserId() {
        return userId;
    }

    public String getNome() {
        return nome;
    }

    public Role getRole() {
        return role;
    }
}

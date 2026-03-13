package org.example.model;

public class Admin extends User {
    public Admin(int id, String nome, String username, String password) {
        super(id, nome, username, password, Role.ADMIN);
    }
}

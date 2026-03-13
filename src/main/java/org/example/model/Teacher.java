package org.example.model;

import java.util.HashSet;
import java.util.Set;

public class Teacher extends User {
    private final Set<Integer> subjectIds = new HashSet<>();

    public Teacher(int id, String nome, String username, String password) {
        super(id, nome, username, password, Role.PROFESSOR);
    }

    public Set<Integer> getSubjectIds() {
        return subjectIds;
    }
}

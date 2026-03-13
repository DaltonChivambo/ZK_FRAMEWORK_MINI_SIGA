package org.example.vm;

import org.example.bootstrap.AppContext;
import org.example.model.Role;
import org.example.model.User;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Messagebox;

import java.util.Optional;

public class LoginViewModel {
    private String username;
    private String password;

    @Command
    @NotifyChange({"username", "password"})
    public void login() {
        Optional<String> token = AppContext.authService().login(username, password);
        if (token.isEmpty()) {
            Messagebox.show("Credenciais invalidas.");
            return;
        }

        User user = AppContext.authService().resolveByToken(token.get()).orElseThrow();
        Session session = Sessions.getCurrent();
        session.setAttribute("authToken", token.get());
        session.setAttribute("authUserId", user.getId());
        if (user.getRole() == Role.ADMIN) {
            Executions.sendRedirect("/admin.zul");
            return;
        }
        if (user.getRole() == Role.PROFESSOR) {
            Executions.sendRedirect("/teacher.zul");
            return;
        }
        Executions.sendRedirect("/student.zul");
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

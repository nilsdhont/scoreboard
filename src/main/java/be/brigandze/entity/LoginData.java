package be.brigandze.entity;

import io.quarkus.runtime.annotations.RegisterForReflection;

// serialized by the JAX-RS client for the login POST body, so native needs explicit registration
@RegisterForReflection
public class LoginData {

    private String username;
    private String password;

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

package model;

import java.util.Objects;

/**
 * Represents an application user with a name and assigned role.
 * @author Valerie H.
 */
public class User {
    /// Name of the User
    private final String name;

    /// Role of the User
    private final String role;

    public User(String name, String role) {
        this.name = name;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(getName(), user.getName()) && Objects.equals(getRole(), user.getRole());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getRole());
    }

    @Override
    public String toString() {
        return "User{name='" + name + "', role='" + role + "'}";
    }
}


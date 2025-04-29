package model;

/**
 * Represents an application user with a name and assigned role.
 */
public record User(
        /// The user's name or identifier.
        String name,

        /// The role granted to the user (e.g., "admin", "user").
        String role) { }

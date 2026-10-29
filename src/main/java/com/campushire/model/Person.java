package com.campushire.model;

/**
 * Abstract base class representing a person in the CampusHire system.
 * Demonstrates OOP abstraction and inheritance.
 */
public abstract class Person {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    protected Person() {}

    protected Person(String id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    // Abstract method — subclasses must describe their role
    public abstract String getRole();

    // Getters and setters (encapsulation)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return getRole() + "[" + id + "] " + getFullName();
    }
}

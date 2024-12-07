package com.micro;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
public class User {

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getProfilePic() {
		return profilePic;
	}

	public void setProfilePic(String profilePic) {
		this.profilePic = profilePic;
	}

	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters.")
    private String name;

//    @Email
    @NotNull(message = "Email cannot be null.")
    @Column(unique = true) // Ensures email uniqueness at the DB level
    private String email;

    @NotNull
    @Size(min = 3, message = "Password must be at least 3 characters long.")
    private String password;

    @NotNull
    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 digits.")
    private String phone;

    @Column(name = "profile_pic", nullable = true)
    private String profilePic; // Stores file path or URL for the profile picture

    @Column(nullable = true)
    private String dept; // Department or specialization (optional)

    @NotNull(message = "Role cannot be null.")
    private String role; // Changed to String for simplicity

    // Constructors, Getters, and Setters
    public User() {
    }

    public User(String name, String email, String password, String phone, String profilePic, String dept, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.profilePic = profilePic;
        this.dept = dept;
        this.role = role;
    }

    // Getters and setters...
}

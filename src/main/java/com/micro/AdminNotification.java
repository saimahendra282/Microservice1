package com.micro;

//import javax.persistence.*;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_notifications") // MySQL table name
public class AdminNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generated ID for MySQL
    private Long id; // Unique identifier for the notification

    @Column(nullable = false)
    private String email; // Email of the recipient (user/peer)

    @Column(nullable = false)
    private String role; // Role of the recipient (user/peer)

    private String department; // Department, if applicable (for peers)
    
    @Column(nullable = false, length = 500) // Max length for the message
    private String message; // The notification message

    @Column(nullable = false)
    private boolean sentAsBulk; // Flag to indicate if this was a bulk notification

    @Column(nullable = false)
    private String senderEmail; // Email of the admin who sent the notification

    @Column(nullable = false)
    private LocalDateTime timestamp; // Timestamp when the notification was sent

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSentAsBulk() {
        return sentAsBulk;
    }

    public void setSentAsBulk(boolean sentAsBulk) {
        this.sentAsBulk = sentAsBulk;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

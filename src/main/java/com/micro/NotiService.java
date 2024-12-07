package com.micro;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class NotiService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JWTManager jwtManager;

    /**
     * Utility method to validate token and extract claims.
     */
    public Map<String, String> validateToken(String token) {
        Map<String, String> validationResponse = jwtManager.validateToken(token);

        if ("404".equals(validationResponse.get("code"))) {
            throw new IllegalArgumentException("Invalid or expired JWT token.");
        }

        return validationResponse;
    }

    /**
     * Store a notification after validating the JWT token.
     */
    public ResponseEntity<String> storeNotification(AdminNotification notification, boolean isBulk, String token) {
        Map<String, String> claims = validateToken(token);

        String senderEmail = claims.get("email");
        String senderRole = claims.get("role");

        if (senderRole == null || (!"admin".equalsIgnoreCase(senderRole) && !"peer".equalsIgnoreCase(senderRole) && !"user".equalsIgnoreCase(senderRole))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized: Only admins, peers, or users can send notifications.");
        }

        notification.setSenderEmail(senderEmail);
        notification.setTimestamp(LocalDateTime.now());

        if (isBulk) {
            notification.setEmail("bulk");
            notification.setRole("bulk");
            notification.setDepartment(null);
            notification.setSentAsBulk(true);
        } else {
            notification.setSentAsBulk(false);
        }

        notificationRepository.save(notification);
        return ResponseEntity.ok("Notification stored successfully!");
    }

    /**
     * Fetch all bulk notifications.
     */
    public List<AdminNotification> getBulkNotifications() {
        return notificationRepository.findByRole("bulk");
    }

    /**
     * Retrieve targeted notifications for a specific user.
     */
    public List<AdminNotification> getTargetedNotifications(String email) {
        return notificationRepository.findByEmail(email);
    }

    /**
     * Fetch all notifications (admin only).
     */
    public ResponseEntity<List<AdminNotification>> getAllNotifications(String token) {
        Map<String, String> claims = validateToken(token);

        String senderRole = claims.get("role");
         

        return ResponseEntity.ok(notificationRepository.findAll());
    }

    /**
     * Fetch peer-specific notifications.
     */
    public List<AdminNotification> getPeerNotifications(String email) {
        return notificationRepository.findByEmailAndRole(email, "peer");
    }

    /**
     * Fetch user-specific notifications.
     */
    public List<AdminNotification> getUserNotifications(String email) {
        return notificationRepository.findByEmailAndRole(email, "user");
    }
}

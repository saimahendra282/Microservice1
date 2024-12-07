package com.micro;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/notifications")
public class NotiController {

    @Autowired
    private NotiService notiService;

    private String extractBearerToken(String token) {
        return token.startsWith("Bearer ") ? token.substring(7) : token;
    }

    /**
     * Store a notification.
     */
    @PostMapping("/store")
    public ResponseEntity<String> storeNotification(
            @RequestBody AdminNotification notification,
            @RequestParam boolean isBulk,
            @RequestHeader("Authorization") String token) {
        return notiService.storeNotification(notification, isBulk, extractBearerToken(token));
    }

    /**
     * Fetch all bulk notifications.
     */
    @GetMapping("/bulk")
    public ResponseEntity<List<AdminNotification>> getBulkNotifications() {
        return ResponseEntity.ok(notiService.getBulkNotifications());
    }

    /**
     * Fetch targeted notifications for a specific user.
     */
    @GetMapping("/targetednoti")
    public ResponseEntity<List<AdminNotification>> getTargetedNotifications(@RequestParam String email) {
        return ResponseEntity.ok(notiService.getTargetedNotifications(email));
    }

    /**
     * Fetch all notifications (admin only).
     */
    @GetMapping("/allnoti")
    public ResponseEntity<List<AdminNotification>> getAllNotifications(@RequestHeader("Authorization") String token) {
        return notiService.getAllNotifications(extractBearerToken(token));
    }

    /**
     * Fetch notifications for a peer.
     */
    @GetMapping("/peernoti")
    public ResponseEntity<List<AdminNotification>> getPeerNotifications(@RequestHeader("Authorization") String token) {
        Map<String, String> claims = notiService.validateToken(extractBearerToken(token));
        return ResponseEntity.ok(notiService.getPeerNotifications(claims.get("email")));
    }

    /**
     * Fetch notifications for a user.
     */
    @GetMapping("/usernoti")
    public ResponseEntity<List<AdminNotification>> getUserNotifications(@RequestHeader("Authorization") String token) {
        Map<String, String> claims = notiService.validateToken(extractBearerToken(token));
        return ResponseEntity.ok(notiService.getUserNotifications(claims.get("email")));
    }
}

package com.micro;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTManager jwtManager;

    // Custom hashing method for password
    private String hashPassword(String password) {
        int hashSeed = 31; // Prime number for hash transformation
        long hashValue = 7; // Initial hash value

        for (char c : password.toCharArray()) {
            hashValue = hashValue * hashSeed + c;
            hashValue = hashValue % Integer.MAX_VALUE; // Keep it within bounds
        }
        return Long.toHexString(hashValue); // Convert to hexadecimal for compactness
    }
    public List<Map<String,String>> getUsers(){
    	List<User> users = userRepository.findByRole("user");
    	List<Map<String, String>> UserBysai = new ArrayList<>();
    	for(User u:users) {
    		Map<String, String> profileData = new HashMap<>();
            profileData.put("email", u.getEmail());
            profileData.put("name", u.getName());
            profileData.put("phone", u.getPhone()); // Leave null values for frontend to handle
            profileData.put("role", "user");
//            profileData.put("dept", peer.getDept());
            profileData.put("profilePic", u.getProfilePic());
            UserBysai.add(profileData);
    	}
    	return UserBysai;
    }
    public List<Map<String, String>> getPeers() {
        List<User> peers = userRepository.findByRole("peer");
        List<Map<String, String>> peersProfileResponse = new ArrayList<>();

        for (User peer : peers) {
            Map<String, String> profileData = new HashMap<>();
            profileData.put("email", peer.getEmail());
            profileData.put("name", peer.getName());
            profileData.put("phone", peer.getPhone()); // Leave null values for frontend to handle
            profileData.put("role", "peer");
            profileData.put("dept", peer.getDept());
            profileData.put("profilePic", peer.getProfilePic());

            peersProfileResponse.add(profileData);
        }

        return peersProfileResponse;
    }


    // Register a user with hashed password
    public String registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return "Email already in use.";
        }

        // Hash the password before saving
        String hashedPassword = hashPassword(user.getPassword());
        user.setPassword(hashedPassword);

        // Save the user with hashed password
        userRepository.save(user);
        return "User registered successfully.";
    }

    // Login a user with hash verification and JWT generation
    public ResponseEntity<Map<String, String>> loginUser(String email, String password) {
        User user = userRepository.findByEmail(email);

        String hashedPassword = hashPassword(password);
        if (user == null || !hashedPassword.equals(user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials."));
        }

        // Generate JWT with additional phone field
        String jwtToken = jwtManager.generateToken(email, user.getRole(), user.getName(), user.getProfilePic(), user.getPhone());

        // Return token and user details in the response
        Map<String, String> response = Map.of(
            "token", jwtToken,
            "role", user.getRole(),
            "name", user.getName(),
            "profilePic", user.getProfilePic(),
            "phone", user.getPhone()
        );

        return ResponseEntity.ok(response);
    }

    // Validate the JWT Token
    public ResponseEntity<Map<String, String>> validateToken(String token) {
        Map<String, String> validationResponse = jwtManager.validateToken(token);

        if ("404".equals(validationResponse.get("code"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", validationResponse.get("error")));
        }

        return ResponseEntity.ok(validationResponse);
    }

    // Role-based authorization example
    public ResponseEntity<String> performAdminAction(String token) {
        if (!hasRole("admin", token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Admin role is required.");
        }

        return ResponseEntity.ok("Admin action performed successfully.");
    }

    public ResponseEntity<String> performPeerAction(String token) {
        if (!hasRole("peer", token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Peer role is required.");
        }

        return ResponseEntity.ok("Peer action performed successfully.");
    }

    public ResponseEntity<String> performUserAction(String token) {
        if (!hasRole("user", token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. User role is required.");
        }

        return ResponseEntity.ok("User action performed successfully.");
    }

    public ResponseEntity<Map<String, String>> getUserProfile(String token) {
        Map<String, String> validationResponse = jwtManager.validateToken(token);

        if ("404".equals(validationResponse.get("code"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", validationResponse.get("error")));
        }

        Map<String, String> profileResponse = new HashMap<>();
        profileResponse.put("email", validationResponse.get("email"));
        profileResponse.put("role", validationResponse.get("role"));
        profileResponse.put("name", validationResponse.get("name"));
        profileResponse.put("profilePic", validationResponse.get("profilePic")); // Retrieve profilePic URL directly
        profileResponse.put("phone", validationResponse.get("phone") != null ? validationResponse.get("phone") : "N/A"); // Handle null phone

        return ResponseEntity.ok(profileResponse);
    }
    public User updateUserByEmail(User user) {
        // Fetch the user based on email
        User existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser == null) {
            throw new NoSuchElementException("User not found.");
        }

        // Update fields only if they are provided
        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }
        if (user.getPhone() != null) {
            existingUser.setPhone(user.getPhone());
        }
        if (user.getProfilePic() != null) {
            existingUser.setProfilePic(user.getProfilePic());
        }

        // Role-specific field updates
        if ("peer".equalsIgnoreCase(existingUser.getRole()) && user.getDept() != null) {
            existingUser.setDept(user.getDept());
        }

        // Update password only if provided and non-empty
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            String hashedPassword = hashPassword(user.getPassword());
            existingUser.setPassword(hashedPassword);
        }

        // Save and return the updated user
        return userRepository.save(existingUser);
    }


    public Map<String, String> updateUser(String email, Map<String, String> updatedData) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        // Update fields based on the provided data
        if (updatedData.containsKey("name")) {
            user.setName(updatedData.get("name"));
        }
        if (updatedData.containsKey("phone")) {
            user.setPhone(updatedData.get("phone"));
        }
        if (updatedData.containsKey("profilePic")) {
            user.setProfilePic(updatedData.get("profilePic"));
        }

        // Save the updated user
        userRepository.save(user);

        // Return the updated user details
        Map<String, String> updatedProfile = new HashMap<>();
        updatedProfile.put("email", user.getEmail());
        updatedProfile.put("name", user.getName());
        updatedProfile.put("phone", user.getPhone());
        updatedProfile.put("role", user.getRole());
        updatedProfile.put("profilePic", user.getProfilePic());

        return updatedProfile;
    }
    public String deleteUser(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        userRepository.delete(user);
        return "User deleted successfully with email: " + email;
    }

 // Update Peer by email
    public User updatePeerByEmail(String email, Map<String, String> updatedData) {
        // Fetch the peer based on email
        User existingPeer = userRepository.findByEmail(email);
        if (existingPeer == null || !"peer".equalsIgnoreCase(existingPeer.getRole())) {
            throw new NoSuchElementException("Peer not found.");
        }

        // Update fields only if they are provided in the map
        if (updatedData.containsKey("name")) {
            existingPeer.setName(updatedData.get("name"));
        }
        if (updatedData.containsKey("phone")) {
            existingPeer.setPhone(updatedData.get("phone"));
        }
        if (updatedData.containsKey("profilePic")) {
            existingPeer.setProfilePic(updatedData.get("profilePic"));
        }

        // Update department only for peers
        if ("peer".equalsIgnoreCase(existingPeer.getRole()) && updatedData.containsKey("dept")) {
            existingPeer.setDept(updatedData.get("dept"));
        }

        // Update password only if provided and non-empty
        if (updatedData.containsKey("password") && !updatedData.get("password").isEmpty()) {
            String hashedPassword = hashPassword(updatedData.get("password"));
            existingPeer.setPassword(hashedPassword);
        }

        // Save and return the updated peer
        return userRepository.save(existingPeer);
    }
    public List<Map<String, String>> getAdmins() {
        List<User> admins = userRepository.findByRole("admin");
        List<Map<String, String>> adminsProfileResponse = new ArrayList<>();

        for (User admin : admins) {
            Map<String, String> profileData = new HashMap<>();
            profileData.put("email", admin.getEmail());
            profileData.put("name", admin.getName());
            profileData.put("phone", admin.getPhone()); // Leave null values for frontend to handle
            profileData.put("role", "admin");
            profileData.put("profilePic", admin.getProfilePic());

            adminsProfileResponse.add(profileData);
        }

        return adminsProfileResponse;
    }

    // Delete Peer by email
    public String deletePeer(String email) {
        User peer = userRepository.findByEmail(email);
        if (peer == null || !"peer".equalsIgnoreCase(peer.getRole())) {
            throw new RuntimeException("Peer not found with email: " + email);
        }

        userRepository.delete(peer);
        return "Peer deleted successfully with email: " + email;
    }

    public boolean hasRole(String role, String token) {
        // Validate the JWT token using JWTManager
        Map<String, String> validationResponse = jwtManager.validateToken(token);

        // Check if the token is valid or not
        if ("404".equals(validationResponse.get("code"))) {
            return false; // Invalid token, return false
        }

        // Compare the role from the token with the provided role
        String tokenRole = validationResponse.get("role");
        return role.equals(tokenRole);  // Return true if roles match
    }

}

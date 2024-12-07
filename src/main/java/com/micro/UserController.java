package com.micro;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@CrossOrigin
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // User Registration Endpoint
    @PostMapping(value = "/register", consumes = "application/json")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        try {
            // Register the user directly with the profile picture URL
            String response = userService.registerUser(user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error registering user.");
        }
    }

    // Update User Profile Endpoint
    @PutMapping(value = "/update-profile", consumes = "application/json")
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        try {
            // Call the service to update the user
            User updatedUser = userService.updateUserByEmail(user);
            return ResponseEntity.ok(updatedUser); // Return updated user data
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating user.");
        }
    }
    @PutMapping("/update-user")
    public ResponseEntity<Map<String, String>> updateUser(@RequestParam String email, @RequestBody Map<String, String> updatedData) {
        try {
            Map<String, String> updatedUser = userService.updateUser(email, updatedData);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    @PutMapping("/update-peer")
    public ResponseEntity<Map<String, String>> updatePeer(@RequestParam String email, @RequestBody Map<String, String> updatedData) {
        try {
            User updatedPeer = userService.updatePeerByEmail(email, updatedData);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Peer updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @DeleteMapping("/delete-peer")
    public ResponseEntity<String> deletePeer(@RequestParam String email) {
        try {
            String response = userService.deletePeer(email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @DeleteMapping("/delete-user")
    public ResponseEntity<String> deleteUser(@RequestParam String email) {
        try {
            String response = userService.deleteUser(email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }



    // User Login Endpoint with JWT Generation
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody Map<String, String> loginDetails) {
        String email = loginDetails.get("email");
        String password = loginDetails.get("password");
        return userService.loginUser(email, password);
    }

    // Token Validation Endpoint
    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, String>> validateToken(@RequestBody Map<String, String> requestBody) {
        String token = requestBody.get("token");
        return userService.validateToken(token);
    }

    // Get User Profile Endpoint
    @PostMapping("/get-profile")
    public ResponseEntity<Map<String, String>> getUserProfile(@RequestBody Map<String, String> requestBody) {
        String token = requestBody.get("token");
        return userService.getUserProfile(token);
    }

    // Get All Peers - Admin only
    @GetMapping("/peers")
    public ResponseEntity<List<Map<String, String>>> getPeers() {
        try {
            List<Map<String, String>> peersProfileResponse = userService.getPeers();

            // Return 200 with empty array if no peers found
            return ResponseEntity.ok(peersProfileResponse);

        } catch (Exception e) {
            e.printStackTrace();

            // Return 500 in case of an exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Collections.emptyList());
        }
    }
@GetMapping("/adminusers")
public ResponseEntity<List<Map<String,String>>> getUsers(){
	try {
		List<Map<String,String>> Userbysai = userService.getUsers();
		return ResponseEntity.ok(Userbysai);
	} catch (Exception e) {
		// TODO: handle exception
		e.printStackTrace();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.emptyList());
	}
}
@GetMapping("/alladmins")
public List<Map<String, String>> getAllAdmins() {
    return userService.getAdmins();
}
    // Example of an action only accessible by an admin (role-based access control)
    @PostMapping("/admin-action")
    public ResponseEntity<String> performAdminAction(@RequestBody Map<String, String> requestBody) {
        String token = requestBody.get("token");
        return userService.performAdminAction(token);
    }

    // Example of an action only accessible by a peer (role-based access control)
    @PostMapping("/peer-action")
    public ResponseEntity<String> performPeerAction(@RequestBody Map<String, String> requestBody) {
        String token = requestBody.get("token");
        return userService.performPeerAction(token);
    }

    // Example of an action only accessible by a user (role-based access control)
    @PostMapping("/user-action")
    public ResponseEntity<String> performUserAction(@RequestBody Map<String, String> requestBody) {
        String token = requestBody.get("token");
        return userService.performUserAction(token);
    }
}

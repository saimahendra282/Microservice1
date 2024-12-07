package com.micro;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTManager {

    public final String SECRET_KEY = "eyJhcGkta2V5IjogImFzbmJ0dWVhb3J1ZW9idTQzNW5zdGF1In0K";
    public final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // Method to generate JWT token with email, role, name, profilePic, and phone
    public String generateToken(String email, String role, String name, String profilePic, String phone) {
        Map<String, Object> claims = new HashMap<>();
        
        // Encrypt email and phone before adding to JWT claims
        claims.put("email", encryptData(email)); 
        claims.put("role", role); 
        claims.put("name", name); 
        claims.put("profilePic", profilePic); 
        
        // Ensure phone is included in the claims and encrypted if it's not null
        if (phone != null) {
            claims.put("phone", encryptData(phone)); 
        }

        return Jwts.builder()
                   .setClaims(claims)
                   .setIssuedAt(new Date()) // Issue time
                   .setExpiration(new Date(new Date().getTime() + 86400000)) // Token valid for 24 hours
                   .signWith(key) // Sign the token with the secret key
                   .compact(); // Generate the token string
    }

    // Method to validate JWT token and extract claims
    public Map<String, String> validateToken(String token) {
        Map<String, String> resp = new HashMap<>();
        try {
            Claims claims = Jwts.parserBuilder()
                                .setSigningKey(key) 
                                .build()
                                .parseClaimsJws(token)
                                .getBody();

            Date expiration = claims.getExpiration();
            if (expiration == null || expiration.before(new Date())) {
                resp.put("code", "404");
                resp.put("error", "JWT token has expired.");
                return resp;
            }

            // Log claims for debugging
            System.out.println("Claims: " + claims);

            // Decrypt email and phone
            resp.put("email", decryptData(claims.get("email", String.class))); 
            resp.put("role", claims.get("role", String.class)); 
            resp.put("name", claims.get("name", String.class)); 
            resp.put("profilePic", claims.get("profilePic", String.class)); 
            resp.put("phone", claims.get("phone") != null ? decryptData(claims.get("phone", String.class)) : null); // Decrypt phone if exists

            resp.put("code", "200");
            return resp;
        } catch (Exception e) {
            resp.put("code", "404");
            resp.put("error", e.getMessage());
            return resp;
        }
    }

    // Encrypt data
    public String encryptData(String data) {
        try {
            if (data == null) {
                return null; // Handle null data
            }

            MessageDigest MD5 = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = MD5.digest("SUNNY".getBytes()); 
            SecretKey cryptoKey = new SecretKeySpec(keyBytes, 0, 16, "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, cryptoKey); 
            byte[] encryptedBytes = cipher.doFinal(data.getBytes()); 
            return Base64.getEncoder().encodeToString(encryptedBytes); 
        } catch (Exception e) {
            return "Encryption error: " + e.getMessage(); // More detailed error message for debugging
        }
    }

    // Decrypt data
    public String decryptData(String data) {
        try {
            if (data == null) {
                return null; // Handle null data
            }

            MessageDigest MD5 = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = MD5.digest("SUNNY".getBytes()); 
            SecretKey cryptoKey = new SecretKeySpec(keyBytes, 0, 16, "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, cryptoKey); 
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(data)); 
            return new String(decryptedBytes); 
        } catch (Exception e) {
            return "Decryption error: " + e.getMessage(); // More detailed error message for debugging
        }
    }
}

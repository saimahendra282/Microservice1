package com.micro;

import com.micro.AdminNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface NotificationRepository extends JpaRepository<AdminNotification, Long> {
    // Custom query methods (if needed) can be added here
	 List<AdminNotification> findByRole(String role);

	    List<AdminNotification> findByEmail(String email);

	    List<AdminNotification> findByEmailAndRole(String email, String role);
	    
	    
}

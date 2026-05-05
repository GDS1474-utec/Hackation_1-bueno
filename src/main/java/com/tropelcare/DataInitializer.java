package com.tropelcare;

import com.tropelcare.guardian.Guardian;
import com.tropelcare.guardian.GuardianRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DataInitializer implements CommandLineRunner {

    private final GuardianRepository guardianRepository;

    @Value("${app.admin.display-name:Cameron Walker}")
    private String adminName;

    @Value("${app.admin.email:cameron@tuckersoft.com}")
    private String adminEmail;

    @Value("${app.admin.notification-email:cameron.real@gmail.com}")
    private String notificationEmail;

    public DataInitializer(GuardianRepository guardianRepository) {
        this.guardianRepository = guardianRepository;
    }

    @Override
    public void run(String... args) {
        if (!guardianRepository.existsByEmail(adminEmail)) {
            Guardian guardian = new Guardian();
            guardian.setDisplayName(adminName);
            guardian.setEmail(adminEmail);
            guardian.setNotificationEmail(notificationEmail);
            guardian.setCreatedAt(Instant.now());
            guardianRepository.save(guardian);
        }
    }
}

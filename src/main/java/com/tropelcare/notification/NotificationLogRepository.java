package com.tropelcare.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findBySignalIdOrderByCreatedAtDesc(Long signalId);
}

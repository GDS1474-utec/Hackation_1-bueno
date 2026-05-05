package com.tropelcare.notification;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/signals")
public class NotificationLogController {
    private final NotificationLogService notificationLogService;

    public NotificationLogController(NotificationLogService notificationLogService) {
        this.notificationLogService = notificationLogService;
    }

    @GetMapping("/{id}/notifications")
    public List<NotificationLogDTO> findBySignalId(@PathVariable Long id) {
        return notificationLogService.findBySignalId(id);
    }
}

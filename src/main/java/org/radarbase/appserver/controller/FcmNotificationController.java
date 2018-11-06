package org.radarbase.appserver.controller;

import org.radarbase.fcm.dto.FcmNotifications;
import org.radarbase.appserver.service.FcmNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FcmNotificationController {

    @Autowired
    private FcmNotificationService notificationService;

    @GetMapping("/notifications")
    public ResponseEntity<FcmNotifications> getAllNotifications() {
        return ResponseEntity.ok(this.notificationService.getAllNotifications());
    }

/*    @GetMapping("/notifications")
    public ResponseEntity<FcmNotifications> getAllNotifications() {
        return ResponseEntity.ok(this.notificationService.getAllNotifications());
    }*/

}

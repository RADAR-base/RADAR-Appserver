package org.radarbase.appserver.controller;

import org.radarbase.appserver.dto.fcm.FcmNotifications;
import org.radarbase.appserver.dto.RadarUserDto;
import org.radarbase.appserver.dto.RadarUsers;
import org.radarbase.appserver.service.FcmNotificationService;
import org.radarbase.appserver.service.RadarUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
public class RadarUserController {
    private static final Logger logger = LoggerFactory.getLogger(RadarUserController.class);

    @Autowired
    private RadarUserService userService;

    @Autowired
    private FcmNotificationService notificationService;

    @PostMapping("/users")
    public ResponseEntity addRadarUser(@RequestParam(value = "projectId") String projectId,
                                       @RequestParam(value = "subjectId") String subjectId,
                                       @RequestParam(value = "sourceId") String sourceId)
            throws URISyntaxException {

        RadarUserDto user = this.userService.storeRadarUser(projectId, subjectId, sourceId);
        return ResponseEntity
                .created(new URI("/user/" + user.getId())).body(user);
    }

    @GetMapping("/users")
    public ResponseEntity<RadarUsers> getAllRadarUsers() {
        return null;
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<RadarUserDto> getRadarUserUsingId(
            @PathVariable String id) {
        return null;
    }

    @GetMapping("/users/{subjectid}")
    public ResponseEntity<RadarUserDto> getRadarUserUsingSubjectId(
            @PathVariable String subjectId) {
        return null;
    }

    @GetMapping("/users/{subjectid}/notifications")
    public ResponseEntity<FcmNotifications> getRadarNotificationsUsingSubjectId(
            @PathVariable String subjectId) {
        return ResponseEntity.ok(this.notificationService.getNotificationsBySubjectId(subjectId));
    }
}

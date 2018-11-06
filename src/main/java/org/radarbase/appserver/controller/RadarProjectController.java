package org.radarbase.appserver.controller;

import org.radarbase.appserver.dto.fcm.FcmNotifications;
import org.radarbase.appserver.dto.RadarProjectDto;
import org.radarbase.appserver.dto.RadarProjects;
import org.radarbase.appserver.dto.RadarUsers;
import org.radarbase.appserver.service.FcmNotificationService;
import org.radarbase.appserver.service.RadarProjectService;
import org.radarbase.appserver.service.RadarUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RadarProjectController {

    @Autowired
    private RadarProjectService projectService;

    @Autowired
    private RadarUserService userService;

    @Autowired
    private FcmNotificationService notificationService;

    @GetMapping("/projects")
    public ResponseEntity<RadarProjects> getAllProjects() {
        return ResponseEntity.ok(this.projectService.getAllProjects());
    }

    @GetMapping("/projects/{id}")
    public ResponseEntity<RadarProjectDto> getProjectsUsingId(@PathVariable String id) {
        return ResponseEntity.ok(this.projectService.getProjectById(id));
    }
    @GetMapping("/projects/{projectid}")
    public ResponseEntity<RadarProjectDto> getProjectsUsingProjectId(@PathVariable String projectId) {
        return ResponseEntity.ok(this.projectService.getProjectById(projectId));
    }

    @GetMapping("/projects/{projectid}/users")
    public ResponseEntity<RadarUsers> getUsersUsingProjectId(@PathVariable String projectId) {
        return ResponseEntity.ok(this.userService.getUserByProjectId(projectId));
    }

    @GetMapping("/projects/{projectid}/notifications")
    public ResponseEntity<FcmNotifications> getNotificationsUsingProjectId(@PathVariable String projectId) {
        return ResponseEntity.ok(this.notificationService.getNotificationsByProjectId(projectId));
    }

    @GetMapping("/projects/{projectid}/users/{subjectid}")
    public ResponseEntity<FcmNotifications> getUsersUsingProjectIdAndSubjectId(@PathVariable String projectId,
                                                             @PathVariable String subjectId) {
        return ResponseEntity.ok(this.notificationService.getNotificationsByProjectIdAndSubjectId(projectId, subjectId));
    }
}

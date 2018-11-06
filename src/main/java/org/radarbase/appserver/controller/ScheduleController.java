package org.radarbase.appserver.controller;

import org.radarbase.appserver.dto.ScheduleNotificationDto;
import org.radarbase.fcm.dto.FcmNotificationDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedule/")
public class ScheduleController {


    @PostMapping("/single")
    public ResponseEntity<FcmNotificationDto> scheduleSingleNotification(
            @RequestBody ScheduleNotificationDto notification) {

        // Call scheduler service to add -> which calls other appropriate services to put in db
        // And also schedules using quarts maybe

        return null;
    }

    @PostMapping("/multiple")
    public ResponseEntity<FcmNotificationDto> scheduleMultipleNotification(
            @RequestBody List<ScheduleNotificationDto> notifications) {

        return null;
    }
}

package org.radarbase.appserver.dto;

import java.io.Serializable;

import java.time.LocalDateTime;

public class RadarNotificationDto implements Serializable {

    private static final long serialVersionUID = 3L;

    private String id;

    private LocalDateTime scheduledTime;

    private boolean delivered;

    private String title;

    private String body;

    private int ttlSeconds;


}

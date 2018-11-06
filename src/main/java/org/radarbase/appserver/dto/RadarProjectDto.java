package org.radarbase.appserver.dto;

import org.radarbase.fcm.dto.FcmUsers;

import java.io.Serializable;

public class RadarProjectDto implements Serializable{

    private static final long serialVersionUID = 2L;

    private String id;

    private String projectId;

    private FcmUsers fcmUsers;

}

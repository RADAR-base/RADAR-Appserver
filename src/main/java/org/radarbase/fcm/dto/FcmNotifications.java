package org.radarbase.fcm.dto;

import java.util.List;

public class FcmNotifications {

    private List<FcmNotificationDto>  notifications;

    public List<FcmNotificationDto> getNotifications() {
        return notifications;
    }

    public FcmNotifications setNotifications(List<FcmNotificationDto> notifications) {
        this.notifications = notifications;
        return this;
    }


}

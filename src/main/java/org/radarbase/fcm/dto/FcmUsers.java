package org.radarbase.fcm.dto;

import java.util.List;

public class FcmUsers {

    private List<FcmUserDto> users;

    public List<FcmUserDto> getUsers() {
        return users;
    }

    public FcmUsers setUsers(List<FcmUserDto> users) {
        this.users = users;
        return this;
    }
}

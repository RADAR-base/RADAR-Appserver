package org.radarbase.appserver.dto;

import java.util.List;

public class RadarUsers {

    private List<RadarUserDto> users;

    public List<RadarUserDto> getUsers() {
        return users;
    }

    public RadarUsers setUsers(List<RadarUserDto> users) {
        this.users = users;
        return this;
    }
}

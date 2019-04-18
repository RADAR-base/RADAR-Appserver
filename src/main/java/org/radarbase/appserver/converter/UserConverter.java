/*
 *
 *  *
 *  *  * Copyright 2018 King's College London
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *  *
 *  *
 *
 */

package org.radarbase.appserver.converter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.radarbase.appserver.dto.fcm.FcmUserDto;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.entity.UserMetrics;

/**
 * Converter {@link Converter} class for {@link User} entity and {@link FcmUserDto} DTO.
 *
 * @author yatharthranjan
 */
public class UserConverter implements Converter<User, FcmUserDto> {

  @Override
  public User dtoToEntity(FcmUserDto fcmUserDto) {

    return new User()
        .setFcmToken(fcmUserDto.getFcmToken())
        .setSubjectId(fcmUserDto.getSubjectId())
        .setUserMetrics(getValidUserMetrics(fcmUserDto))
        .setEnrolmentDate(fcmUserDto.getEnrolmentDate())
        .setTimezone(fcmUserDto.getTimezone())
        .setLanguage(fcmUserDto.getLanguage());
  }

  @Override
  public FcmUserDto entityToDto(User user) {
    return new FcmUserDto(user);
  }

  public static UserMetrics getValidUserMetrics(FcmUserDto fcmUserDto) {
    UserMetrics userMetrics;
    if (fcmUserDto.getLastOpened() == null && fcmUserDto.getLastDelivered() == null) {
      userMetrics = new UserMetrics(LocalDateTime.now().toInstant(ZoneOffset.UTC), null);
    } else if (fcmUserDto.getLastDelivered() == null) {
      userMetrics = new UserMetrics(fcmUserDto.getLastOpened(), null);
    } else if (fcmUserDto.getLastOpened() == null) {
      userMetrics =
          new UserMetrics(
              LocalDateTime.now().toInstant(ZoneOffset.UTC),
              fcmUserDto.getLastDelivered());
    } else {
      userMetrics =
          new UserMetrics(
              fcmUserDto.getLastOpened(),
              fcmUserDto.getLastDelivered());
    }

    return userMetrics;
  }
}

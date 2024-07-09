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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.radarbase.appserver.dto.fcm.FcmUserDto;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.entity.UserMetrics;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Converter {@link Converter} class for {@link User} entity and {@link FcmUserDto} DTO.
 *
 * @author yatharthranjan
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class UserConverter implements Converter<User, FcmUserDto> {

  public static UserMetrics getValidUserMetrics(FcmUserDto fcmUserDto) {
    Instant lastOpened = fcmUserDto.getLastOpened();
    if (lastOpened == null) {
      lastOpened = Instant.now();
    }
    return new UserMetrics(lastOpened, fcmUserDto.getLastDelivered());
  }

  @Override
  public User dtoToEntity(FcmUserDto fcmUserDto) {

    return new User()
        .setFcmToken(fcmUserDto.getFcmToken())
        .setSubjectId(fcmUserDto.getSubjectId())
        .setEmailAddress(fcmUserDto.getEmailAddress())
        .setUserMetrics(getValidUserMetrics(fcmUserDto))
        .setEnrolmentDate(fcmUserDto.getEnrolmentDate())
        .setTimezone(fcmUserDto.getTimezone())
        .setLanguage(fcmUserDto.getLanguage());
  }

  @Override
  public FcmUserDto entityToDto(User user) {
    return new FcmUserDto(user);
  }
}

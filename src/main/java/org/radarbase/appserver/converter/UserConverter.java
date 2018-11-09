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

import org.radarbase.appserver.entity.User;
import org.radarbase.fcm.dto.FcmUserDto;

/**
 * @author yatharthranjan
 */
public class UserConverter extends Converter<User, FcmUserDto> {

    @Override
    public User dtoToEntity(FcmUserDto fcmUserDto) {
        return null;
    }

    @Override
    public FcmUserDto entityToDto(User user) {
        return new FcmUserDto(user);
    }
}

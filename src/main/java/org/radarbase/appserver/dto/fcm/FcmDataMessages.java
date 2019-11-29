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

package org.radarbase.appserver.dto.fcm;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yatharthranjan
 */
@Getter
@EqualsAndHashCode
@ToString
public class FcmDataMessages {

    private static final Logger logger = LoggerFactory.getLogger(FcmDataMessages.class);

    @Size(max = 200)
    private List<FcmDataMessageDto> dataMessages;

    public FcmDataMessages() {
        dataMessages = new ArrayList<>();
    }

    public FcmDataMessages setDataMessages(List<FcmDataMessageDto> dataMessages) {
        this.dataMessages = dataMessages;
        return this;
    }

    public FcmDataMessages addDataMessage(FcmDataMessageDto dataMessageDto) {

        if (!dataMessages.contains(dataMessageDto)) {
            this.dataMessages.add(dataMessageDto);
        } else {
            logger.info("Data message {} already exists in the Fcm Data Messages.", dataMessageDto);
        }
        return this;
    }
}

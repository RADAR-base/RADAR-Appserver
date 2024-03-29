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

package org.radarbase.appserver.service.questionnaire.protocol.factory;

import org.radarbase.appserver.service.TaskService;
import org.radarbase.appserver.service.questionnaire.protocol.ProtocolHandler;
import org.radarbase.appserver.service.questionnaire.protocol.RandomRepeatQuestionnaireHandler;
import org.radarbase.appserver.service.questionnaire.protocol.SimpleRepeatQuestionnaireHandler;

public class RepeatQuestionnaireHandlerFactory {

    public static ProtocolHandler getRepeatQuestionnaireHandler(RepeatQuestionnaireHandlerType name) {
        switch (name) {
            case SIMPLE:
                return new SimpleRepeatQuestionnaireHandler();
            case RANDOM:
                return new RandomRepeatQuestionnaireHandler();
            default:
                return new SimpleRepeatQuestionnaireHandler();
        }
    }

}

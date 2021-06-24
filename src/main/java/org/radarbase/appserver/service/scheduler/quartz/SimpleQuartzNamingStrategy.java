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

package org.radarbase.appserver.service.scheduler.quartz;

import org.springframework.stereotype.Component;

@Component
public class SimpleQuartzNamingStrategy implements QuartzNamingStrategy {

  private static final String TRIGGER_PREFIX = "message-trigger-";
  private static final String JOB_PREFIX = "message-jobdetail-";

  @Override
  public String getTriggerName(String userName, String messageId) {
    return new StringBuilder(TRIGGER_PREFIX)
        .append(userName)
        .append('-')
        .append(messageId)
        .toString();
  }

  @Override
  public String getJobKeyName(String userName, String messageId) {
    return new StringBuilder(JOB_PREFIX).append(userName).append('-').append(messageId).toString();
  }

  @Override
  public String getMessageId(String key) {
    String[] keys = key.split("-");
    return keys[keys.length - 1];
  }
}

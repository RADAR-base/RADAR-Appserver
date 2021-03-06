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

package org.radarbase.appserver.service.scheduler;

/**
 * Class for scheduling periodic jobs for notifying admins in case of any unwanted state.
 *
 * @author yatharthranjan
 */
public class AdminEmailNotifierScheduler {

  // TODO: Add a scheduler that checks everyday if a user is inactive for a long time (lastOpened >
  // 30 days) and send a warning email to the study/project admin.
  // TODO: We can also add the functionality to remind the users to open the app by sending a push
  // notification.
}

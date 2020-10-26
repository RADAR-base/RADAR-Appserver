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

package org.radarbase.fcm.downstream;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidConfig.Priority;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FcmOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.radarbase.fcm.model.FcmDataMessage;
import org.radarbase.fcm.model.FcmDownstreamMessage;
import org.radarbase.fcm.model.FcmNotificationMessage;

/**
 * When authorizing via a service account, you have to set the GOOGLE_APPLICATION_CREDENTIALS
 * environment variable. For More info, see
 * https://firebase.google.com/docs/admin/setup#initialize-sdk
 *
 * @author yatharthranjan
 */
@Slf4j
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class AdminSdkFcmSender implements FcmSender {

  public AdminSdkFcmSender() throws IOException {
    // TODO also take config from application properties
    FirebaseOptions options =
        new FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.getApplicationDefault())
            .build();

    try {
      FirebaseApp.initializeApp(options);
    } catch (IllegalStateException exc) {
      log.warn("Firebase app was already initialised. {}", exc.getMessage());
    }
  }

  @Override
  public void send(FcmDownstreamMessage downstreamMessage) throws FirebaseMessagingException {
    // TODO Add support for APNS and WebPush as well
    String priority = downstreamMessage.getPriority();

    Message.Builder message =
        Message.builder()
            .setToken(downstreamMessage.getTo())
            .setFcmOptions(FcmOptions.builder().build())
            .setCondition(downstreamMessage.getCondition());

    if (downstreamMessage instanceof FcmNotificationMessage) {
      FcmNotificationMessage notificationMessage = (FcmNotificationMessage) downstreamMessage;
      message
          .setAndroidConfig(
              AndroidConfig.builder()
                  .setCollapseKey(downstreamMessage.getCollapseKey())
                  .setPriority(priority == null ? Priority.HIGH : Priority.valueOf(priority))
                  .setTtl(downstreamMessage.getTimeToLive())
                  .setNotification(getAndroidNotification(notificationMessage))
                  .putAllData(notificationMessage.getData())
                  .build())
          .putAllData(notificationMessage.getData())
          .setNotification(
              Notification.builder()
                  .setBody(
                      String.valueOf(
                          notificationMessage.getNotification().getOrDefault("body", "")))
                  .setTitle(
                      String.valueOf(
                          notificationMessage.getNotification().getOrDefault("title", "")))
                  .setImage(
                      String.valueOf(
                          notificationMessage.getNotification().getOrDefault("image_url", "")))
                  .build());
    } else if (downstreamMessage instanceof FcmDataMessage) {
      FcmDataMessage dataMessage = (FcmDataMessage) downstreamMessage;
      message
          .setAndroidConfig(
              AndroidConfig.builder()
                  .setCollapseKey(downstreamMessage.getCollapseKey())
                  .setPriority(priority == null ? Priority.NORMAL : Priority.valueOf(priority))
                  .setTtl(downstreamMessage.getTimeToLive())
                  .putAllData(dataMessage.getData())
                  .build())
          .putAllData(dataMessage.getData());
    } else {
      throw new IllegalArgumentException(
          "The Message type is not known." + downstreamMessage.getClass());
    }

    String response = FirebaseMessaging.getInstance().send(message.build());
    log.info("Message Sent with response : {}", response);
  }

  private AndroidNotification getAndroidNotification(FcmNotificationMessage notificationMessage) {
    AndroidNotification.Builder builder =
        AndroidNotification.builder()
            .setBody(String.valueOf(notificationMessage.getNotification().getOrDefault("body", "")))
            .setTitle(
                String.valueOf(notificationMessage.getNotification().getOrDefault("title", "")))
            .setChannelId(
                getString(notificationMessage.getNotification().get("android_channel_id")))
            .setColor(getString(notificationMessage.getNotification().get("color")))
            .setTag(getString(notificationMessage.getNotification().get("tag")))
            .setIcon(getString(notificationMessage.getNotification().get("icon")))
            .setSound(getString(notificationMessage.getNotification().get("sound")))
            .setClickAction(getString(notificationMessage.getNotification().get("click_action")));

    String bodyLocKey = getString(notificationMessage.getNotification().get("body_loc_key"));
    String titleLocKey = getString(notificationMessage.getNotification().get("title_loc_key"));

    if (bodyLocKey != null) {
      builder
          .setBodyLocalizationKey(
              getString(notificationMessage.getNotification().get("body_loc_key")))
          .addBodyLocalizationArg(
              getString(notificationMessage.getNotification().get("body_loc_args")));
    }

    if (titleLocKey != null) {
      builder
          .addTitleLocalizationArg(
              getString(notificationMessage.getNotification().get("title_loc_args")))
          .setTitleLocalizationKey(
              getString(notificationMessage.getNotification().get("title_loc_key")));
    }

    return builder.build();
  }

  private String getString(Object obj) {
    return obj == null ? null : String.valueOf(obj);
  }

  @Override
  public boolean doesProvideDeliveryReceipt() {
    return false;
  }
}

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

package org.radarbase.fcm.config;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/** Util class for back off strategy */
@Slf4j
public class BackOffStrategy {

  private static final int DEFAULT_RETRIES = 3;
  private static final long DEFAULT_WAIT_TIME_IN_MILLI = 1000;

  private int numberOfRetries;
  private int numberOfTriesLeft;
  private long defaultTimeToWait;
  private long timeToWait;
  private Random random = new Random();

  public BackOffStrategy() {
    this(DEFAULT_RETRIES, DEFAULT_WAIT_TIME_IN_MILLI);
  }

  BackOffStrategy(int numberOfRetries, long defaultTimeToWait) {
    this.numberOfRetries = numberOfRetries;
    this.numberOfTriesLeft = numberOfRetries;
    this.defaultTimeToWait = defaultTimeToWait;
    this.timeToWait = defaultTimeToWait;
  }

  /** @return true if there are tries left */
  boolean shouldRetry() {
    return numberOfTriesLeft > 0;
  }

  public void errorOccured2() throws Exception {
    numberOfTriesLeft--;
    if (!shouldRetry()) {
      throw new Exception(
          "Retry Failed: Total of attempts: "
              + numberOfRetries
              + ". Total waited time: "
              + timeToWait
              + "ms.");
    }
    waitUntilNextTry();
    timeToWait *= 2;
    // we add a random time (recommendation from google)
    timeToWait += random.nextInt(500);
  }

  void errorOccured() {
    numberOfTriesLeft--;
    if (!shouldRetry()) {
      log.info(
          "Retry Failed: Total of attempts: {}. Total waited time: {} ms.",
          numberOfRetries,
          timeToWait);
    }
    waitUntilNextTry();
    timeToWait *= 2;
    // we add a random time (google recommendation)
    timeToWait += random.nextInt(500);
  }

  private void waitUntilNextTry() {
    try {
      Thread.sleep(timeToWait);
    } catch (InterruptedException e) {
      log.info("Error waiting until next try for the backoff strategy. Error: {}", e.getMessage());
    }
  }

  public long getTimeToWait() {
    return this.timeToWait;
  }

  /** Use this method when the call was successful otherwise it will continue in an infinite loop */
  void doNotRetry() {
    numberOfTriesLeft = 0;
  }

  /**
   * Reset back off state. Call this method after successful attempts if you want to reuse the
   * class.
   */
  public void reset() {
    this.numberOfTriesLeft = numberOfRetries;
    this.timeToWait = defaultTimeToWait;
  }
}

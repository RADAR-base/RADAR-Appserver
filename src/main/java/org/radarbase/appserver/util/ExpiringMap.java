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

package org.radarbase.appserver.util;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class ExpiringMap<S, T> {
  private final transient Consumer<Map<S, T>> consumer;
  private final transient int maxSize;
  private final transient Duration expiry;
  private final transient long flushAfter;
  private transient Instant lastFlush;
  private transient Map<S, T> cache;

  /**
   * @param consumer The consumer to consume the contents of the map once the maxSize,expiry or
   *     flushAfter has reached.
   * @param maxSize The maxSize after which contents need to be flushed
   * @param expiry The Duration in seconds after which the records need to be flushed even if
   *     maxSize is not yet reached.
   * @param flushAfter The amount in seconds after which to flush in case of no activity (i.e no new
   *     content is added)
   */
  public ExpiringMap(Consumer<Map<S, T>> consumer, int maxSize, Duration expiry, long flushAfter) {
    this.consumer = consumer;
    this.maxSize = maxSize;
    this.expiry = expiry;
    this.lastFlush = Instant.MIN;
    this.flushAfter = flushAfter;
    cache = new ConcurrentHashMap<>();
    ScheduledExecutorService executorService =
        Executors.newSingleThreadScheduledExecutor(
            new ExceptionThreadFactory(new LogAndContinueExceptionHandler()));
    executorService.scheduleAtFixedRate(
        this::checkForDeadContent, this.flushAfter, this.flushAfter, TimeUnit.SECONDS);
  }

  public void add(S key, T value) {
    synchronized (this) {
      if (this.lastFlush.plus(expiry).isBefore(Instant.now()) || this.cache.size() >= maxSize) {
        this.flush();
      }
      cache.put(key, value);
    }
  }

  public synchronized T get(S key) {
    T value = cache.get(key);
    if (value == null) {
      throw new NoSuchElementException("The key" + key + "does not exist in the cache.");
    }
    return value;
  }

  private synchronized void checkForDeadContent() {
    if (!cache.isEmpty()) {
      log.info("Flushing content after flushAfter({}) s.", this.flushAfter);
      this.flush();
    }
  }

  private synchronized void flush() {
    Map<S, T> copy = new ConcurrentHashMap<>(cache);
    cache = new ConcurrentHashMap<>();
    this.lastFlush = Instant.now();
    consumer.accept(copy);
  }

  public static class ExceptionThreadFactory implements ThreadFactory {
    private static final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
    private final Thread.UncaughtExceptionHandler handler;

    public ExceptionThreadFactory(Thread.UncaughtExceptionHandler handler) {
      this.handler = handler;
    }

    @Override
    public Thread newThread(@NotNull Runnable run) {
      Thread thread = defaultFactory.newThread(run);
      thread.setUncaughtExceptionHandler(handler);
      return thread;
    }
  }

  public static class LogAndContinueExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger logger =
        LoggerFactory.getLogger(LogAndContinueExceptionHandler.class);

    @Override
    public void uncaughtException(Thread thread, Throwable t) {
      logger.warn("The thread {} in Expiring Map threw an exception.", thread, t);
    }
  }
}

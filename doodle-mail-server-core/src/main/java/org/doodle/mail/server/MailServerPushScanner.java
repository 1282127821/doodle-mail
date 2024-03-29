/*
 * Copyright (c) 2022-present Doodle. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.doodle.mail.server;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MailServerPushScanner implements SmartLifecycle {
  ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
  MailServerPushService pushService;
  MailServerProperties properties;

  @Override
  public void start() {
    log.info(
        "启动推送邮件扫描任务, 延迟: {}秒, 间隔: {} 秒",
        properties.getPush().getScanDelay(),
        properties.getPush().getScanInterval());
    executorService.scheduleAtFixedRate(
        pushService::scanSchedules,
        properties.getPush().getScanDelay(),
        properties.getPush().getScanInterval(),
        TimeUnit.SECONDS);
  }

  @Override
  public void stop() {
    executorService.shutdownNow();
  }

  @Override
  public boolean isRunning() {
    return false;
  }
}

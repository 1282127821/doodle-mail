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

import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.doodle.design.mail.MailErrorCode;
import org.doodle.design.mail.MailState;
import org.doodle.design.mail.model.info.MailTargetInfo;
import org.springframework.util.CollectionUtils;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MailServerPushService {
  MailServerPushRepo pushRepo;
  MailServerContentService contentService;
  MailServerDeliverService deliverService;
  MailServerProperties properties;

  public void push(MailServerPushEntity pushEntity) {
    MailServerContentEntity contentEntity =
        contentService.findOrElseThrow(pushEntity.getContentId());
    pushEntity.setState(MailState.SENDING);
    pushRepo.save(pushEntity);
    MailTargetInfo targetInfo = pushEntity.getTargetInfo();
    MailErrorCode errorCode =
        deliverService.deliver(
            targetInfo.getRoleId(), targetInfo.getRoute(), List.of(contentEntity));
    if (errorCode == MailErrorCode.FAILURE) {
      if (pushEntity.getRetryTime() >= properties.getPush().getMaxRetryTime()) {
        pushEntity.setState(MailState.DIE);
      } else {
        if (pushEntity.getSendTime() != null) {
          pushEntity.setRetryTime(pushEntity.getRetryTime() + 1);
        }
        pushEntity.setState(MailState.RETRYING);
      }
    } else {
      pushEntity.setState(MailState.COMPLETED);
    }
    pushEntity.setSendTime(Instant.now());
    pushRepo.save(pushEntity);
  }

  public void scan() {
    List<MailServerPushEntity> retryingEntity = pushRepo.findAllByState(MailState.RETRYING);
    if (!CollectionUtils.isEmpty(retryingEntity)) {
      log.info("扫描到需要重试推送任务: {}", retryingEntity);
      retryingEntity.forEach(r -> r.setState(MailState.READY));
      pushRepo.saveAll(retryingEntity);
    }
  }
}

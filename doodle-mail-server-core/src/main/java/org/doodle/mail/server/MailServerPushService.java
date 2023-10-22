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
import org.doodle.design.mail.MailScheduleState;
import org.doodle.design.mail.MailState;
import org.doodle.design.mail.model.info.MailTargetInfo;
import org.springframework.util.CollectionUtils;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MailServerPushService {
  MailServerPushRepo pushRepo;
  MailServerPushScheduleRepo scheduleRepo;
  MailServerContentService contentService;
  MailServerDeliverService deliverService;
  MailServerProperties properties;

  public void schedule(MailServerPushScheduleEntity scheduleEntity) {
    pushRepo
        .findById(scheduleEntity.getPushId())
        .ifPresentOrElse(
            pushEntity -> schedule(scheduleEntity, pushEntity),
            () -> scheduleRepo.delete(scheduleEntity));
  }

  private void schedule(
      MailServerPushScheduleEntity scheduleEntity, MailServerPushEntity pushEntity) {
    if (pushEntity.getState() != MailState.SCHEDULING) {
      log.info("删除推送调度, 当前推送邮件状态: {}", pushEntity.getState());
      scheduleRepo.delete(scheduleEntity);
    }

    contentService
        .findById(pushEntity.getContentId())
        .ifPresentOrElse(
            contentEntity -> schedule(scheduleEntity, pushEntity, contentEntity),
            () -> scheduleRepo.delete(scheduleEntity));
  }

  private void schedule(
      MailServerPushScheduleEntity scheduleEntity,
      MailServerPushEntity pushEntity,
      MailServerContentEntity contentEntity) {

    MailTargetInfo targetInfo = pushEntity.getTargetInfo();
    MailErrorCode errorCode =
        deliverService.deliver(
            targetInfo.getRoleId(), targetInfo.getRoute(), List.of(contentEntity));
    if (errorCode == MailErrorCode.FAILURE) {
      if (scheduleEntity.getRetryTime() >= properties.getPush().getMaxRetryTime()) {
        log.info("邮件推送调度已达到最大重试次数, 进入IDLE状态并删除调度. {}", scheduleEntity);
        pushEntity.setState(MailState.DIE);
        scheduleRepo.delete(scheduleEntity);
      } else {
        if (pushEntity.getSendTime() != null) {
          scheduleEntity.setRetryTime(scheduleEntity.getRetryTime() + 1);
        }
        scheduleEntity.setState(MailScheduleState.IDLE);
        scheduleRepo.save(scheduleEntity);
      }
    } else {
      log.info("邮件推送调度已完成并删除. {}", scheduleEntity);
      pushEntity.setState(MailState.COMPLETED);
      scheduleRepo.delete(scheduleEntity);
    }
    pushEntity.setSendTime(Instant.now());
    pushRepo.save(pushEntity);
  }

  public void schedule(MailServerPushEntity pushEntity) {
    scheduleRepo
        .findByPushId(pushEntity.getPushId())
        .ifPresentOrElse(
            scheduleEntity -> log.info("邮件调度已经开始: {}", scheduleEntity),
            () -> scheduleNew(pushEntity));
  }

  private void scheduleNew(MailServerPushEntity pushEntity) {
    log.info("推送邮件调度: {}", pushEntity);
    MailServerPushScheduleEntity scheduleEntity =
        MailServerPushScheduleEntity.builder()
            .pushId(pushEntity.getPushId())
            .state(MailScheduleState.SENDING)
            .build();
    scheduleRepo.save(scheduleEntity);
  }

  public void scanSchedules() {
    List<MailServerPushScheduleEntity> scheduleEntities =
        scheduleRepo.findAllByState(MailScheduleState.IDLE);
    if (!CollectionUtils.isEmpty(scheduleEntities)) {
      log.info("扫描到需要重试推送任务: {}", scheduleEntities);
      scheduleEntities.forEach(schedule -> schedule.setState(MailScheduleState.SENDING));
      scheduleRepo.saveAll(scheduleEntities);
    }
  }
}

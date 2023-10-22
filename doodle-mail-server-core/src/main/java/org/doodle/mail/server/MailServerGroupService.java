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

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.doodle.design.mail.MailErrorCode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MailServerGroupService extends MailServerSeqService {
  MailServerGroupRepo groupRepo;
  MailServerRoleService roleService;
  MailServerContentService contentService;
  MailServerDeliverService deliverService;
  Executor executor;

  public MailServerGroupService(
      MongoTemplate mongoTemplate,
      MailServerGroupRepo groupRepo,
      MailServerRoleService roleService,
      MailServerContentService contentService,
      MailServerDeliverService deliverService,
      Executor executor) {
    super(mongoTemplate, MailServerGroupEntity.COLLECTION);
    this.groupRepo = groupRepo;
    this.roleService = roleService;
    this.contentService = contentService;
    this.deliverService = deliverService;
    this.executor = executor;
  }

  public Mono<Void> syncMono(String roleId, long roleCreateTime, Object route) {
    return Mono.fromRunnable(() -> sync(roleId, roleCreateTime, route));
  }

  void sync(String roleId, long roleCreateTime, Object route) {
    MailServerRoleSyncEntity roleSyncEntity = roleService.findOrElseCreate(roleId);
    List<MailServerGroupEntity> groupList = groupRepo.findAll();
    if (!CollectionUtils.isEmpty(groupList)) {
      List<String> contentIds = new ArrayList<>();
      for (MailServerGroupEntity group : groupList) {
        if (group.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                > roleCreateTime
            && group.getGroupId() < roleSyncEntity.getSyncId()) {
          contentIds.add(group.getContentId());
        }
        if (group.getGroupId() > roleSyncEntity.getSyncId()) {
          roleSyncEntity.setSyncId(group.getGroupId());
        }
      }
      if (!CollectionUtils.isEmpty(contentIds)) {
        List<MailServerContentEntity> contents = contentService.findAllById(contentIds);
        if (!CollectionUtils.isEmpty(contents)) {
          roleService.save(roleSyncEntity);
          executor.execute(
              () -> {
                MailErrorCode errorCode = deliverService.deliver(roleId, route, contents);
                if (errorCode == MailErrorCode.FAILURE) {
                  log.error("给玩家 {} 推送 GROUP 邮件发生错误", roleId);
                }
              });
        }
      }
    }
  }
}

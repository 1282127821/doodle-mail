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

import java.util.List;
import org.doodle.design.mail.MailContentInfo;
import org.doodle.design.mail.MailContentInfoList;
import org.doodle.design.mail.MailMapper;
import org.springframework.util.CollectionUtils;

public class MailServerMapper extends MailMapper {

  public MailContentInfo toProto(MailServerContentEntity contentEntity) {
    return MailContentInfo.newBuilder()
        .setContentId(contentEntity.getContentId())
        .setTitle(contentEntity.getTitle())
        .setContent(contentEntity.getContent())
        .setAttachment(contentEntity.getAttachment())
        .build();
  }

  public org.doodle.design.mail.model.info.MailContentInfo toPojo(
      MailServerContentEntity contentEntity) {
    return org.doodle.design.mail.model.info.MailContentInfo.builder()
        .contentId(contentEntity.getContentId())
        .title(contentEntity.getTitle())
        .content(contentEntity.getContent())
        .attachment(contentEntity.getAttachment())
        .build();
  }

  public MailContentInfoList toContentInfoList(List<MailServerContentEntity> contentEntities) {
    MailContentInfoList.Builder builder = MailContentInfoList.newBuilder();
    if (!CollectionUtils.isEmpty(contentEntities)) {
      contentEntities.stream().map(this::toProto).forEach(builder::addContent);
    }
    return builder.build();
  }
}

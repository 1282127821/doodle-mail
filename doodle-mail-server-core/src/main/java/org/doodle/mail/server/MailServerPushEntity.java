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
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.doodle.design.mail.MailState;
import org.doodle.design.mail.model.info.MailTargetInfo;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Builder
@ToString
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = MailServerPushEntity.COLLECTION)
public class MailServerPushEntity {
  public static final String COLLECTION = "mail-pushs";

  @MongoId String pushId;
  MailTargetInfo targetInfo;
  String contentId;
  MailState state = MailState.PENDING;
  Instant sendTime;
  long retryTime;

  @CreatedDate LocalDateTime createdAt;
}

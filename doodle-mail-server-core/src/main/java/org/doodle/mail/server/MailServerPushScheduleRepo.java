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
import java.util.Optional;
import org.doodle.design.mail.MailScheduleState;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailServerPushScheduleRepo
    extends MongoRepository<MailServerPushScheduleEntity, String> {
  List<MailServerPushScheduleEntity> findAllByState(MailScheduleState state);

  Optional<MailServerPushScheduleEntity> findByPushId(String pushId);
}

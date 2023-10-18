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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MailServerRoleService {
  MailServerRoleSyncRepo roleSyncRepo;

  public MailServerRoleSyncEntity findOrElseCreate(String roleId) {
    return roleSyncRepo
        .findById(roleId)
        .orElseGet(() -> MailServerRoleSyncEntity.builder().roleId(roleId).build());
  }

  public void save(MailServerRoleSyncEntity roleSyncEntity) {
    roleSyncRepo.save(roleSyncEntity);
  }
}

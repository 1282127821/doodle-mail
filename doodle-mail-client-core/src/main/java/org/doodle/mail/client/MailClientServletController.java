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
package org.doodle.mail.client;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.doodle.design.common.Result;
import org.doodle.design.mail.MailDeliverOps;
import org.doodle.design.mail.MailErrorCode;
import org.doodle.design.mail.model.payload.request.MailDeliverRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MailClientServletController implements MailDeliverOps.Servlet {
  MailClientDeliverHandler.Servlet deliverHandler;

  @PostMapping(MailDeliverOps.Servlet.DELIVER_MAPPING)
  @Override
  public Result<MailErrorCode> deliver(MailDeliverRequest request) {
    return Result.ok(deliverHandler.apply(request));
  }

  @MessageExceptionHandler(Exception.class)
  ResponseEntity<Result<Void>> onDeliverException(Exception ignored) {
    return ResponseEntity.badRequest().body(Result.bad());
  }
}

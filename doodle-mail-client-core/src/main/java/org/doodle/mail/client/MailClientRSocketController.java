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
import org.doodle.design.mail.MailDeliverOps;
import org.doodle.design.mail.MailDeliverReply;
import org.doodle.design.mail.MailDeliverRequest;
import org.doodle.design.mail.MailErrorCode;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MailClientRSocketController implements MailDeliverOps.RSocket {
  MailClientMapper mapper;
  MailClientDeliverHandler.RSocket deliverHandler;

  @MessageMapping(MailDeliverOps.RSocket.DELIVER_MAPPING)
  @Override
  public Mono<MailDeliverReply> deliver(MailDeliverRequest request) {
    return Mono.fromCallable(() -> deliverHandler.apply(request)).map(mapper::toDeliverReply);
  }

  @MessageExceptionHandler(Exception.class)
  Mono<MailErrorCode> onDeliverException(Exception ignored) {
    return Mono.just(MailErrorCode.FAILURE);
  }
}

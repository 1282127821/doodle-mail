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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.doodle.broker.client.BrokerClientRSocketRequester;
import org.doodle.design.broker.frame.BrokerFrame;
import org.doodle.design.broker.frame.BrokerFrameMimeTypes;
import org.doodle.design.broker.frame.BrokerFrameUtils;
import org.doodle.design.mail.*;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MailServerRSocketDeliverHandler implements MailServerDeliverHandler {
  BrokerClientRSocketRequester requester;
  MailServerMapper mapper;

  @Override
  public MailDeliverRoute.RouteMethodCase routeMethod() {
    return MailDeliverRoute.RouteMethodCase.RSOCKET;
  }

  @Override
  public MailErrorCode deliver(
      String roleId, Object route, List<MailServerContentEntity> contentEntities) {
    MailDeliverRoute deliverRoute = (MailDeliverRoute) route;
    MailDeliverRequest deliverRequest =
        MailDeliverRequest.newBuilder()
            .setRoleId(roleId)
            .setContent(mapper.toContentInfoList(contentEntities))
            .build();
    BrokerFrame frame = BrokerFrameUtils.unicast(deliverRoute.getRsocket().getTagsMap());
    return requester
        .route(MailDeliverOps.RSocket.DELIVER_MAPPING)
        .metadata(frame, BrokerFrameMimeTypes.BROKER_FRAME_MIME_TYPE)
        .data(deliverRequest)
        .retrieveMono(MailErrorCode.class)
        .blockOptional()
        .orElse(MailErrorCode.FAILURE);
  }
}

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
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.doodle.design.mail.MailDeliverRoute;
import org.doodle.design.mail.MailErrorCode;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MailServerDeliverService {
  Map<MailDeliverRoute.RouteMethodCase, MailServerDeliverHandler> deliverHandlers;

  public MailErrorCode deliver(
      String roleId, Object route, List<MailServerContentEntity> contentEntities) {
    MailServerDeliverHandler deliverHandler = findDeliverHandler(route);
    return Objects.nonNull(deliverHandler)
        ? deliverHandler.deliver(roleId, route, contentEntities)
        : MailErrorCode.FAILURE;
  }

  MailServerDeliverHandler findDeliverHandler(Object route) {
    if (route instanceof MailDeliverRoute deliverRoute) {
      return deliverRoute.hasRsocket()
          ? deliverHandlers.get(MailDeliverRoute.RouteMethodCase.RSOCKET)
          : deliverHandlers.get(MailDeliverRoute.RouteMethodCase.SERVLET);
    } else if (route instanceof org.doodle.design.mail.model.info.MailDeliverRoute deliverRoute) {
      return Objects.nonNull(deliverRoute.getRsocket())
          ? deliverHandlers.get(MailDeliverRoute.RouteMethodCase.RSOCKET)
          : deliverHandlers.get(MailDeliverRoute.RouteMethodCase.SERVLET);
    }
    return null;
  }
}

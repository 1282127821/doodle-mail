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
package org.doodle.mail.autoconfigure.client;

import org.doodle.broker.client.BrokerClientRSocketRequester;
import org.doodle.mail.client.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(MailClientProperties.class)
@EnableConfigurationProperties(MailClientProperties.class)
public class MailClientAutoConfiguration {

  @AutoConfiguration
  @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
  public static class ServletConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public MailClientServlet mailClientServlet(RestTemplateBuilder builder) {
      return new MailClientServletImpl(builder.build());
    }

    @Bean
    @ConditionalOnMissingBean
    public MailClientServletController mailClientServletController(
        ObjectProvider<MailClientDeliverHandler.Servlet> provider) {
      return new MailClientServletController(provider.getIfUnique());
    }
  }

  @AutoConfiguration
  @ConditionalOnClass(BrokerClientRSocketRequester.class)
  @ConditionalOnBean(BrokerClientRSocketRequester.class)
  public static class RSocketConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public MailClientRSocket mailClientRSocket(
        BrokerClientRSocketRequester requester, MailClientProperties properties) {
      return new BrokerMailClientRSocket(requester, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public MailClientRSocketController mailClientRSocketController(
        ObjectProvider<MailClientDeliverHandler.RSocket> provider) {
      return new MailClientRSocketController(provider.getIfUnique());
    }
  }
}

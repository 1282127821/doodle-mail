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
package org.doodle.mail.autoconfigure.server;

import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.doodle.broker.autoconfigure.client.BrokerClientAutoConfiguration;
import org.doodle.broker.client.BrokerClientRSocketRequester;
import org.doodle.mail.server.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@AutoConfiguration(after = BrokerClientAutoConfiguration.class)
@ConditionalOnClass(MailServerProperties.class)
@EnableConfigurationProperties(MailServerProperties.class)
@EnableMongoAuditing
@EnableMongoRepositories(
    basePackageClasses = {
      MailServerContentRepo.class,
      MailServerGroupRepo.class,
      MailServerRoleSyncRepo.class,
      MailServerPushRepo.class
    })
public class MailServerAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public MailServerMapper mailServerMapper() {
    return new MailServerMapper();
  }

  @Bean
  @ConditionalOnMissingBean
  public MailServerRoleService mailServerRoleService(MailServerRoleSyncRepo roleSyncRepo) {
    return new MailServerRoleService(roleSyncRepo);
  }

  @Bean
  @ConditionalOnMissingBean
  public MailServerContentService mailServerContentService(MailServerContentRepo contentRepo) {
    return new MailServerContentService(contentRepo);
  }

  @Bean
  @ConditionalOnMissingBean
  public MailServerDeliverService mailServerDeliverService(
      ObjectProvider<MailServerDeliverHandler> provider) {
    return new MailServerDeliverService(
        provider
            .orderedStream()
            .collect(Collectors.toMap(MailServerDeliverHandler::routeMethod, (v) -> v)));
  }

  @Bean
  @ConditionalOnMissingBean
  public MailServerPushService mailServerPushService(
      MailServerPushRepo pushRepo,
      MailServerContentService contentService,
      MailServerDeliverService deliverService,
      MailServerProperties properties) {
    return new MailServerPushService(pushRepo, contentService, deliverService, properties);
  }

  @Bean
  @ConditionalOnMissingBean
  public MailServerPushListener mailServerPushListener(MailServerPushService pushService) {
    return new MailServerPushListener(pushService);
  }

  @Bean
  @ConditionalOnMissingBean
  public MailServerPushScanner mailServerPushScanner(
      MailServerPushService pushService, MailServerProperties properties) {
    return new MailServerPushScanner(pushService, properties);
  }

  @Bean
  @ConditionalOnMissingBean
  public MailServerGroupService mailServerGroupService(
      MailServerGroupRepo groupRepo,
      MailServerRoleService roleService,
      MailServerContentService contentService,
      MailServerDeliverService deliverService,
      MailServerProperties properties) {
    return new MailServerGroupService(
        groupRepo,
        roleService,
        contentService,
        deliverService,
        Executors.newFixedThreadPool(properties.getDeliver().getThreadNum()));
  }

  @Bean
  @ConditionalOnMissingBean
  public MailServerGroupSeqService mailServerGroupSeqService(MongoTemplate mongoTemplate) {
    return new MailServerGroupSeqService(mongoTemplate);
  }

  @Bean
  @ConditionalOnMissingBean
  public MailServerGroupListener mailServerGroupListener(
      MailServerGroupSeqService groupSeqService) {
    return new MailServerGroupListener(groupSeqService);
  }

  @AutoConfiguration
  @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
  public static class ServletConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MailServerServletDeliverHandler mailServerServletDeliverHandler() {
      return new MailServerServletDeliverHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public MailServerGroupServletController mailServerGroupServletController(
        MailServerGroupService groupService) {
      return new MailServerGroupServletController(groupService);
    }
  }

  @AutoConfiguration
  @ConditionalOnClass(BrokerClientRSocketRequester.class)
  @ConditionalOnBean(BrokerClientRSocketRequester.class)
  public static class RSocketConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public MailServerRSocketDeliverHandler mailServerRSocketDeliverHandler(
        BrokerClientRSocketRequester requester, MailServerMapper mapper) {
      return new MailServerRSocketDeliverHandler(requester, mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public MailServerGroupRSocketController mailServerGroupRSocketController(
        MailServerMapper mapper, MailServerGroupService groupService) {
      return new MailServerGroupRSocketController(mapper, groupService);
    }
  }
}

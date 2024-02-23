/*
 * Copyright 2018 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.messaging.client;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.kit.datamanager.configuration.RabbitMQConfiguration;
import edu.kit.datamanager.entities.messaging.DataResourceMessage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 *
 * @author jejkal
 */
@SpringBootApplication
public class Application {

  private static final Logger LOG = LoggerFactory.getLogger(Application.class);

  @Bean
  public RabbitMQConfiguration configuration(){
    return new RabbitMQConfiguration();
  }

  public static void main(String[] args) {
    SpringApplication application;
    ConfigurableEnvironment environment = new StandardEnvironment();
    if(args.length > 0) {
      environment.setActiveProfiles("cli");
      application = new SpringApplication(CommandLineApplication.class);
    } else {
      environment.setActiveProfiles("scheduled");
      application = new SpringApplication(ClientApplication.class);
    }
    application.setEnvironment(environment);
    application.setWebApplicationType(WebApplicationType.NONE);
    application.run(args);
  }
}
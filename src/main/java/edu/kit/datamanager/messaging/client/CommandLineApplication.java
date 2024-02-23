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
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jejkal
 */
@Configuration
@ComponentScan({"edu.kit.datamanager.messaging.client"})
@EnableAutoConfiguration
@Profile("cli")
public class CommandLineApplication implements ApplicationRunner {

  private static final Logger LOG = LoggerFactory.getLogger(CommandLineApplication.class);

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Override
  public void run(ApplicationArguments args) throws Exception{
    //queue, routing key, message.json
    if(!args.getOptionNames().contains("exchange")){
      System.err.print("Argument --exchange is missing. You must provide an exchange the message is sent to.");
      System.exit(1);
    }

    if(!args.getOptionNames().contains("routingKey")){
      System.err.print("Argument --routingKey is missing. You must provide a routing key used to distribute the message.");
      System.exit(1);
    }

    if(!args.getOptionNames().contains("message")){
      System.err.print("Argument --message is missing. You must provide the path to a json file containing the message to send.");
      System.exit(1);
    }

    Path filePath = Paths.get(args.getOptionValues("message").get(0));

    if(!Files.exists(filePath) || !Files.isRegularFile(filePath)){
      System.err.print("Path argument " + filePath + " does not point to a regular file.");
      System.exit(1);
    }

    byte[] message = FileUtils.readFileToByteArray(filePath.toFile());

    rabbitTemplate.convertAndSend(args.getOptionValues("exchange").get(0), args.getOptionValues("routingKey").get(0), message);

    System.exit(0);
  }

  private void sendDataMessage(String resourceId, DataResourceMessage.ACTION action, Map<String, String> properties, List<String> addressees) throws Exception{
    DataResourceMessage msg = DataResourceMessage.createSubCategoryMessage(resourceId, action, DataResourceMessage.SUB_CATEGORY.DATA, properties, "CommandlineMessager", "CommandlineMessager");
    if(addressees != null && !addressees.isEmpty()){
      msg.getAddressees().addAll(addressees);
    }
    sendMessage(msg);
  }

  private void sendMetadataMessage(String resourceId, DataResourceMessage.ACTION action, List<String> addressees) throws Exception{
    DataResourceMessage msg = DataResourceMessage.createMessage(resourceId, action, "CommandlineMessager", "CommandlineMessager");
    if(addressees != null && !addressees.isEmpty()){
      msg.getAddressees().addAll(addressees);
    }
    sendMessage(msg);
  }

  private void sendMessage(DataResourceMessage msg) throws JsonProcessingException{

    rabbitTemplate.convertAndSend("repository_events", msg.getRoutingKey(), msg.toJson().getBytes());
  }
}
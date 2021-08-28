package com.netflix.conductor.contribs.publisher;

import com.netflix.conductor.core.execution.TaskStatusListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SystemPropertiesPublisherConfiguration.class)
@ConditionalOnProperty(name = "conductor.task-status-listener.type", havingValue = "task_publisher")
public class TaskStatusPublisherConfiguration {


    @Bean
    public TaskStatusListener getTaskStatusListener(RestClientManager rcm) {

        return new TaskStatusPublisher(rcm);
    }
}

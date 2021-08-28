package com.netflix.conductor.contribs.publisher;


import com.netflix.conductor.core.listener.WorkflowStatusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SystemPropertiesPublisherConfiguration.class)
@ConditionalOnProperty(name = "conductor.workflow-status-listener.type", havingValue = "workflow_publisher")
public class WorkflowStatusPublisherConfiguration {

    private static final Logger log = LoggerFactory.getLogger(WorkflowStatusPublisherConfiguration.class);
    @Bean
    public WorkflowStatusListener getWorkflowStatusListener(RestClientManager rcm) {

        return new WorkflowStatusPublisher(rcm);
    }
}

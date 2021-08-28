package com.netflix.conductor.contribs.publisher;

//import com.netflix.conductor.core.config.SystemPropertiesConfiguration;

//public class SystemPropertiesPublisherConfiguration extends SystemPropertiesConfiguration implements PublisherConfiguration{

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("conductor.status-listener.publisher")
public class SystemPropertiesPublisherConfiguration implements PublisherConfiguration{
}

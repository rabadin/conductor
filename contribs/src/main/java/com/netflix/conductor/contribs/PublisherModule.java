/**
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package com.netflix.conductor.contribs;

import com.google.inject.AbstractModule;
import com.netflix.conductor.contribs.publisher.*;
import com.netflix.conductor.core.execution.TaskStatusListener;
//import com.netflix.conductor.core.execution.WorkflowStatusListener;

public class PublisherModule extends AbstractModule {

	@Override
	protected void configure() {
		PublisherConfiguration configuration = new SystemPropertiesPublisherConfiguration();

		bind(PublisherConfiguration.class).to(SystemPropertiesPublisherConfiguration.class);
		bind(WorkflowStatusListener.class).to(WorkflowStatusPublisher.class);
		bind(TaskStatusListener.class).to(TaskStatusPublisher.class);

		new RestClientManager(configuration);
	}
}

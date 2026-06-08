/*
 * Copyright 2026 Haulmont.
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

package io.jmix.core.impl.metadata;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * Installs metadata-generation propagation on supported Spring-managed executors.
 * <p>
 * This post-processor attaches {@link MetadataGenerationTaskDecorator} to
 * {@link ThreadPoolTaskExecutor} beans so asynchronous tasks keep the metadata generation
 * that was visible when they were submitted.
 */
@Component("core_MetadataGenerationExecutorBeanPostProcessor")
public class MetadataGenerationExecutorBeanPostProcessor implements BeanPostProcessor {

    protected final MetadataGenerationTaskDecorator taskDecorator;

    /**
     * Creates a post-processor that propagates metadata generation scopes through Spring executors.
     *
     * @param metadataGenerationManagerProvider provider that resolves the generation manager lazily when tasks are decorated
     */
    public MetadataGenerationExecutorBeanPostProcessor(
            ObjectProvider<MetadataGenerationManager> metadataGenerationManagerProvider) {
        this.taskDecorator = new MetadataGenerationTaskDecorator(metadataGenerationManagerProvider);
    }

    /**
     * Installs the metadata-generation task decorator on supported executor beans before initialization.
     *
     * @param bean bean instance being initialized
     * @param beanName Spring bean name
     * @return processed bean
     * @throws BeansException if bean post-processing fails
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ThreadPoolTaskExecutor executor) {
            executor.setTaskDecorator(taskDecorator);
        }
        return bean;
    }
}

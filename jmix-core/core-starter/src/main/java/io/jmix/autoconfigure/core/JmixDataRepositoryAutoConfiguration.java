/*
 * Copyright 2021 Haulmont.
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

package io.jmix.autoconfigure.core;

import io.jmix.core.DataManager;
import io.jmix.core.impl.repository.support.JmixRepositoryConfigurationExtension;
import io.jmix.core.impl.repository.support.JmixRepositoryFactoryBean;
import io.jmix.core.repository.EnableJmixDataRepositories;
import io.jmix.core.repository.JmixDataRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Jmix data repositories.
 * <p>
 * Activates when:
 * <ul>
 *     <li>there is a bean of type {@link DataManager} configured in the context,</li>
 *     <li>{@link JmixDataRepository} type is on the classpath,</li>
 *     <li>no other {@link JmixDataRepository} configured,</li>
 *     <li>"{@code jmix.core.dataRepositories.enabled}" property is missing or having value: "true".</li>
 * </ul>
 * <p>
 * <p>
 * Once in effect, the auto-configuration is the equivalent of enabling Jmix repositories
 * using the {@link EnableJmixDataRepositories @EnableJmixDataRepositories} annotation.
 */

@AutoConfiguration
@ConditionalOnBean(DataManager.class)
@ConditionalOnClass(JmixDataRepository.class)
@ConditionalOnMissingBean({JmixRepositoryFactoryBean.class, JmixRepositoryConfigurationExtension.class})
@ConditionalOnProperty(prefix = "jmix.core.data-repositories", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import(BootJmixRepositoriesRegistrar.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class JmixDataRepositoryAutoConfiguration {
}

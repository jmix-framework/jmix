/*
 * Copyright 2023 Haulmont.
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

package io.jmix.autoconfigure.flowui.cluster;

import com.hazelcast.core.HazelcastInstance;
import io.jmix.flowui.sys.cluster.AppEventSubscribableChannelSupplier;
import io.jmix.flowui.sys.cluster.impl.NoOpAppEventSubscribableChannelSupplier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

@AutoConfiguration
@ConditionalOnMissingBean(AppEventSubscribableChannelSupplier.class)
@AutoConfigureAfter(HazelcastAutoConfiguration.class)
@Import({AppEventChannelAutoConfiguration.HazelcastChannelConfiguration.class,
        AppEventChannelAutoConfiguration.NoOpChannelConfiguration.class})
public class AppEventChannelAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(HazelcastInstance.class)
    @ConditionalOnSingleCandidate(HazelcastInstance.class)
    @Order(10)
    public static class HazelcastChannelConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public AppEventSubscribableChannelSupplier appEventSubscribableChannelSupplier(
                HazelcastInstance hazelcastInstance) {
            return new HazelcastAppEventSubscribableChannelSupplier(hazelcastInstance);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Order
    public static class NoOpChannelConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public AppEventSubscribableChannelSupplier appEventSubscribableChannelSupplier() {
            return new NoOpAppEventSubscribableChannelSupplier();
        }
    }
}

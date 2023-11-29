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

package io.jmix.autoconfigure.core.cluster;

import com.hazelcast.core.HazelcastInstance;
import io.jmix.core.cluster.ClusterApplicationEventChannelSupplier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnMissingBean(ClusterApplicationEventChannelSupplier.class)
@AutoConfigureAfter(HazelcastAutoConfiguration.class)
@ConditionalOnClass(HazelcastInstance.class)
@ConditionalOnSingleCandidate(HazelcastInstance.class)
public class ClusterApplicationEventChannelAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ClusterApplicationEventChannelSupplier clusterApplicationEventChannelSupplier(
            HazelcastInstance hazelcastInstance) {
        return new HazelcastApplicationEventChannelSupplier(hazelcastInstance);
    }
}

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

package io.jmix.autoconfigure.core.cluster;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.ITopic;
import io.jmix.core.cluster.ClusterApplicationEventChannelSupplier;
import io.jmix.core.cluster.LocalApplicationEventChannelSupplier;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.messaging.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClusterApplicationEventChannelAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ClusterApplicationEventChannelAutoConfiguration.class,
                    LocalApplicationEventChannelAutoConfiguration.class));

    @Test
    public void createsHazelcastChannelSupplierWhenHazelcastIsAvailable() {
        contextRunner.withBean(HazelcastInstance.class, this::hazelcastInstance)
                .run(context -> {
                    assertThat(context).hasSingleBean(ClusterApplicationEventChannelSupplier.class);
                    assertThat(context).getBean(ClusterApplicationEventChannelSupplier.class)
                            .isInstanceOf(HazelcastApplicationEventChannelSupplier.class);
                });
    }

    @Test
    public void createsLocalChannelSupplierWhenHazelcastIsUnavailable() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ClusterApplicationEventChannelSupplier.class);
            assertThat(context).getBean(ClusterApplicationEventChannelSupplier.class)
                    .isInstanceOf(LocalApplicationEventChannelSupplier.class);
        });
    }

    @Test
    public void backsOffWhenCustomChannelSupplierExists() {
        contextRunner.withBean(ClusterApplicationEventChannelSupplier.class, LocalApplicationEventChannelSupplier::new)
                .withBean(HazelcastInstance.class, this::hazelcastInstance)
                .run(context -> {
                    assertThat(context).hasSingleBean(ClusterApplicationEventChannelSupplier.class);
                    assertThat(context).getBean(ClusterApplicationEventChannelSupplier.class)
                            .isInstanceOf(LocalApplicationEventChannelSupplier.class);
                });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private HazelcastInstance hazelcastInstance() {
        HazelcastInstance hazelcastInstance = mock(HazelcastInstance.class);
        ITopic<Message<?>> topic = mock(ITopic.class);
        when(hazelcastInstance.<Message<?>>getTopic(anyString())).thenReturn(topic);
        return hazelcastInstance;
    }
}

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

package io.jmix.autoconfigure.sessions;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.spring.session.HazelcastIndexedSessionRepository;
import com.hazelcast.spring.session.SessionMapCustomizer;
import io.jmix.sessions.SessionsProperties;
import io.jmix.sessions.impl.JmixExpiringSessionMap;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.session.autoconfigure.SessionAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Import;
import org.springframework.session.FlushMode;
import org.springframework.session.MapSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SaveMode;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SessionsAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SessionAutoConfiguration.class,
                    JmixHazelcastSessionsAutoConfiguration.class, TestSessionsRepositoryAutoConfiguration.class))
            .withPropertyValues("jmix.sessions.expiring-map.cleanup-enabled=false");

    private final WebApplicationContextRunner jmixOnlyContextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JmixHazelcastSessionsAutoConfiguration.class,
                    TestSessionsRepositoryAutoConfiguration.class))
            .withPropertyValues("jmix.sessions.expiring-map.cleanup-enabled=false");

    @Test
    public void createsHazelcastSessionRepositoryWhenHazelcastIsAvailable() {
        contextRunner.withBean(HazelcastInstance.class, () -> hazelcast().hazelcastInstance)
                .run(context -> {
                    assertThat(context).hasSingleBean(SessionRepository.class);
                    assertThat(context).getBean(SessionRepository.class)
                            .isInstanceOf(HazelcastIndexedSessionRepository.class);
                    assertThat(context).doesNotHaveBean(JmixExpiringSessionMap.class);
                });
    }

    @Test
    public void createsMapSessionRepositoryWhenHazelcastIsUnavailable() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(SessionRepository.class);
            assertThat(context).getBean(SessionRepository.class).isInstanceOf(MapSessionRepository.class);
            assertThat(context).hasSingleBean(JmixExpiringSessionMap.class);
        });
    }

    @Test
    public void appliesSessionTimeoutFromSessionTimeoutBean() {
        contextRunner.withPropertyValues("spring.session.timeout=42s")
                .run(context -> {
                    MapSessionRepository repository = context.getBean(MapSessionRepository.class);
                    MapSession session = repository.createSession();

                    assertThat(session.getMaxInactiveInterval()).isEqualTo(Duration.ofSeconds(42));
                });
    }

    @Test
    public void fallsBackToServletContextSessionTimeout() {
        jmixOnlyContextRunner.withInitializer(context -> context.getServletContext().setSessionTimeout(7))
                .run(context -> {
                    MapSessionRepository repository = context.getBean(MapSessionRepository.class);
                    MapSession session = repository.createSession();

                    assertThat(session.getMaxInactiveInterval()).isEqualTo(Duration.ofMinutes(7));
                });
    }

    @Test
    public void hazelcastRepositoryFallsBackToServletContextSessionTimeout() {
        jmixOnlyContextRunner.withBean(HazelcastInstance.class, () -> hazelcast().hazelcastInstance)
                .withInitializer(context -> context.getServletContext().setSessionTimeout(7))
                .run(context -> {
                    HazelcastIndexedSessionRepository repository =
                            context.getBean(HazelcastIndexedSessionRepository.class);

                    assertThat(ReflectionTestUtils.getField(repository, "defaultMaxInactiveInterval"))
                            .isEqualTo(Duration.ofMinutes(7));
                });
    }

    @Test
    public void appliesHazelcastSessionRepositoryCustomizer() {
        contextRunner.withBean(HazelcastInstance.class, () -> hazelcast().hazelcastInstance)
                .withPropertyValues("spring.session.timeout=42s")
                .withBean(SessionRepositoryCustomizer.class,
                        () -> (SessionRepositoryCustomizer<HazelcastIndexedSessionRepository>) repository -> {
                            repository.setSessionMapName("customizer:sessions");
                            repository.setFlushMode(FlushMode.IMMEDIATE);
                            repository.setSaveMode(SaveMode.ALWAYS);
                        })
                .run(context -> {
                    HazelcastIndexedSessionRepository repository =
                            context.getBean(HazelcastIndexedSessionRepository.class);

                    assertThat(ReflectionTestUtils.getField(repository, "defaultMaxInactiveInterval"))
                            .isEqualTo(Duration.ofSeconds(42));
                    assertThat(ReflectionTestUtils.getField(repository, "sessionMapName"))
                            .isEqualTo("customizer:sessions");
                    assertThat(ReflectionTestUtils.getField(repository, "flushMode")).isEqualTo(FlushMode.IMMEDIATE);
                    assertThat(ReflectionTestUtils.getField(repository, "saveMode")).isEqualTo(SaveMode.ALWAYS);
                });
    }

    @Test
    public void disablesSessionMapAutoConfigurationUsingSessionRepositoryCustomizer() {
        TestHazelcast hazelcast = hazelcast();

        contextRunner.withBean(HazelcastInstance.class, () -> hazelcast.hazelcastInstance)
                .withBean(SessionRepositoryCustomizer.class,
                        () -> (SessionRepositoryCustomizer<HazelcastIndexedSessionRepository>)
                                HazelcastIndexedSessionRepository::disableSessionMapAutoConfiguration)
                .run(context -> {
                    assertThat(context).hasSingleBean(HazelcastIndexedSessionRepository.class);

                    verify(hazelcast.config, never()).addMapConfig(any(MapConfig.class));
                });
    }

    @Test
    public void appliesSessionMapCustomizer() {
        TestHazelcast hazelcast = hazelcast();

        contextRunner.withBean(HazelcastInstance.class, () -> hazelcast.hazelcastInstance)
                .withBean(SessionMapCustomizer.class, () -> mapConfig -> mapConfig.setTimeToLiveSeconds(123))
                .run(context -> {
                    ArgumentCaptor<MapConfig> mapConfigCaptor = ArgumentCaptor.forClass(MapConfig.class);

                    assertThat(context).hasSingleBean(HazelcastIndexedSessionRepository.class);

                    verify(hazelcast.config).addMapConfig(mapConfigCaptor.capture());
                    assertThat(mapConfigCaptor.getValue().getTimeToLiveSeconds()).isEqualTo(123);
                });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private TestHazelcast hazelcast() {
        HazelcastInstance hazelcastInstance = mock(HazelcastInstance.class);
        Config config = mock(Config.class);
        IMap sessions = mock(IMap.class);
        when(hazelcastInstance.getConfig()).thenReturn(config);
        when(config.addMapConfig(any(MapConfig.class))).thenReturn(config);
        when(hazelcastInstance.getMap(anyString())).thenReturn(sessions);
        when(sessions.addEntryListener(any(), eq(true))).thenReturn(UUID.randomUUID());
        return new TestHazelcast(hazelcastInstance, config);
    }

    private static class TestHazelcast {

        private final HazelcastInstance hazelcastInstance;
        private final Config config;

        private TestHazelcast(HazelcastInstance hazelcastInstance, Config config) {
            this.hazelcastInstance = hazelcastInstance;
            this.config = config;
        }
    }

    @AutoConfiguration(after = {JmixHazelcastSessionsAutoConfiguration.class, SessionAutoConfiguration.class})
    @Import(SessionsAutoConfiguration.SessionRepositoryAutoConfiguration.class)
    @EnableConfigurationProperties(SessionsProperties.class)
    static class TestSessionsRepositoryAutoConfiguration {
    }
}

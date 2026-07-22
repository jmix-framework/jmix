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

package flowui_data_autoconfig;

import io.jmix.aitoolsflowui.service.AiChatService;
import io.jmix.autoconfigure.aitools.AiToolsAutoConfiguration;
import io.jmix.autoconfigure.aitoolsflowuidata.AiToolsFlowuiDataAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that {@code jmix-aitools-flowui-data-starter} behaves as a pure implementations provider:
 * it neither bootstraps the FlowUI layer nor registers its persistent chat implementations unless the
 * core starter ({@code jmix-aitools-starter}) is on the classpath — so omitting the core starter
 * yields a clean context start rather than an opaque missing-bean failure.
 *
 * <p>The absence of the core starter is simulated by hiding its {@link AiToolsAutoConfiguration}
 * marker class with a {@link FilteredClassLoader} (it is a {@code testImplementation} dependency).
 */
public class AiToolsFlowuiDataAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void testPersistentImplsNotRegisteredWithoutCore() {
        contextRunner
                .withClassLoader(new FilteredClassLoader(AiToolsAutoConfiguration.class))
                .withConfiguration(AutoConfigurations.of(AiToolsFlowuiDataAutoConfiguration.class))
                .run(context -> {
                    // No opaque UnsatisfiedDependencyException: the context must start cleanly.
                    assertThat(context).hasNotFailed();
                    // The persistent implementations are gated off when the core starter is absent.
                    assertThat(context).doesNotHaveBean(AiToolsFlowuiDataAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(AiChatService.class);
                });
    }
}

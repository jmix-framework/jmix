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

package grid_data_autoconfig;

import io.jmix.aitools.AiExclusionSupport;
import io.jmix.aitoolsflowui.gridexport.AiGridDataSupport;
import io.jmix.autoconfigure.aitoolsflowui.AiToolsGridExportAutoConfiguration;
import io.jmix.core.Messages;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Verifies that sending grid data to the AI is wired only when the grid export add-on is present, so an
 * application without it starts cleanly instead of failing on the missing types.
 */
public class AiToolsGridExportAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiToolsGridExportAutoConfiguration.class))
            .withInitializer(AiToolsGridExportAutoConfigurationTest::registerCollaborators);

    @Test
    void gridDataSupport_withGridExport_registered() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(AiGridDataSupport.class);
            assertThat(context).hasBean("aitls_AiGridDataSupport");
        });
    }

    @Test
    void gridDataSupport_withoutGridExport_notRegistered() {
        contextRunner
                .withClassLoader(new FilteredClassLoader("io.jmix.autoconfigure.flowuiexport",
                        "io.jmix.gridexportflowui"))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(AiGridDataSupport.class);
                });
    }

    @Test
    void gridDataSupport_applicationOverride_kept() {
        AiGridDataSupport custom = new AiGridDataSupport();

        contextRunner
                .withInitializer(context -> context.getBeanFactory().registerSingleton("customGridDataSupport", custom))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(AiGridDataSupport.class);
                    assertThat(context.getBean(AiGridDataSupport.class)).isSameAs(custom);
                });
    }

    /**
     * Registers the collaborators as ready singletons, so that the injection points of the mocked production classes
     * are not resolved.
     */
    private static void registerCollaborators(ConfigurableApplicationContext context) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        beanFactory.registerSingleton("aiExclusionSupport", mock(AiExclusionSupport.class));
        beanFactory.registerSingleton("messages", mock(Messages.class));
    }
}

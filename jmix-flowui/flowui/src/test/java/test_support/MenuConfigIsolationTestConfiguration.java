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

package test_support;

import io.jmix.flowui.view.template.impl.ViewTemplateDefinitions;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.Collections;

/**
 * Test configuration that disables template-generated menu items.
 */
@TestConfiguration
public class MenuConfigIsolationTestConfiguration {

    /**
     * Returns a template definition registry with no definitions.
     *
     * @return mocked template definition registry
     */
    @Bean
    @Primary
    public ViewTemplateDefinitions viewTemplateDefinitions() {
        ViewTemplateDefinitions definitions = Mockito.mock(ViewTemplateDefinitions.class);
        Mockito.when(definitions.getDefinitions()).thenReturn(Collections.emptyList());
        return definitions;
    }
}

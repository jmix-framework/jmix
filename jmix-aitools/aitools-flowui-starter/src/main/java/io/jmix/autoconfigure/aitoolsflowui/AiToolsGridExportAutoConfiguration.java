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

package io.jmix.autoconfigure.aitoolsflowui;

import io.jmix.aitoolsflowui.gridexport.AiGridDataSupport;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Registers the beans that send data grid rows to the AI chat. Active only when the grid export add-on is on the
 * classpath, which is referenced by name because this starter does not depend on it.
 */
@AutoConfiguration(after = AiToolsFlowuiAutoConfiguration.class)
@ConditionalOnClass(name = "io.jmix.autoconfigure.flowuiexport.GridExportFlowuiAutoConfiguration")
public class AiToolsGridExportAutoConfiguration {

    @Bean("aitls_AiGridDataSupport")
    @ConditionalOnMissingBean
    public AiGridDataSupport aiGridDataSupport() {
        return new AiGridDataSupport();
    }
}

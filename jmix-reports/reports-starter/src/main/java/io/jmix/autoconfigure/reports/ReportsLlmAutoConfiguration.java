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

package io.jmix.autoconfigure.reports;

import io.jmix.aitools.dataload.generation.EntityDataLoadGenerationService;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.libintegration.LlmDataLoader;
import io.jmix.reports.llm.LlmDataQueryService;
import io.jmix.reports.llm.impl.AiToolsLlmDataQueryService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Declares the beans of the {@link DataSetType#LLM} data set type. They exist only
 * when the AI Tools add-on provides its data-load subsystem — the add-on is an optional dependency of
 * Reports, and its data-load part can also be switched off by a property.
 * <p>
 * {@code afterName} keeps this starter free of a dependency on the AI Tools starter while still letting the
 * add-on's beans be defined before {@link ConditionalOnBean} is evaluated.
 */
@AutoConfiguration(afterName = "io.jmix.autoconfigure.aitools.AiToolsDataLoadAutoConfiguration")
@ConditionalOnClass(EntityDataLoadGenerationService.class)
@ConditionalOnBean(EntityDataLoadGenerationService.class)
public class ReportsLlmAutoConfiguration {

    @Bean("report_LlmDataQueryService")
    @ConditionalOnMissingBean(LlmDataQueryService.class)
    public LlmDataQueryService llmDataQueryService() {
        return new AiToolsLlmDataQueryService();
    }

    @Bean("report_LlmDataLoader")
    @ConditionalOnMissingBean(LlmDataLoader.class)
    public LlmDataLoader llmDataLoader() {
        return new LlmDataLoader();
    }
}

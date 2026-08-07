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

package llm_designer.test_support;

import io.jmix.reports.llm.LlmDataQueryService;
import io.jmix.reportsflowui.ReportsFlowuiTestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * The Reports UI test setup plus the service bean that an application gets from the AI Tools add-on, standing in
 * for what {@code ReportsLlmAutoConfiguration} declares at run time.
 */
@Configuration
@Import(ReportsFlowuiTestConfiguration.class)
public class LlmDesignerTestConfiguration {

    @Bean
    public LlmDataQueryService testLlmDataQueryService() {
        return new TestLlmDataQueryService();
    }

    @Bean
    public LlmReportUtil llmReportUtil() {
        return new LlmReportUtil();
    }
}

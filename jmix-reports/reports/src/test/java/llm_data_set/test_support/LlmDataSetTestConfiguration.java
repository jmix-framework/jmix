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

package llm_data_set.test_support;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Wires the LLM data set support the way an application does, but on top of {@link TestLlmDataQueryService} for
 * generation and of {@link TestLlmDataLoader} for the one step that would reach a database.
 */
@Configuration
public class LlmDataSetTestConfiguration {

    @Bean
    public TestLlmDataQueryService testLlmDataQueryService() {
        return new TestLlmDataQueryService();
    }

    /**
     * Replaces the loader Reports declares. Marked primary because the loader is an ordinary Reports bean now:
     * the factory injects it by type, and this one has to win.
     */
    @Bean
    @Primary
    public TestLlmDataLoader testLlmDataLoader() {
        return new TestLlmDataLoader();
    }
}

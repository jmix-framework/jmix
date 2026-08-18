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

import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmDataQueryService;
import io.jmix.reports.llm.LlmQueryExecutionRequest;
import io.jmix.reports.llm.LlmQueryGenerationRequest;

import java.util.List;
import java.util.Map;

/**
 * Stands in for the add-on-backed service, so the designer can be tested without an LLM. It reports generation as
 * available by default; tests may keep the bean present while switching that capability off.
 */
public class TestLlmDataQueryService implements LlmDataQueryService {

    public static final String GENERATED_JPQL = "select o.number as orderNumber from sales_Order o";

    protected boolean generationAvailable = true;

    @Override
    public boolean isGenerationAvailable() {
        return generationAvailable;
    }

    /**
     * Stands in for an add-on whose beans are there while the model they talk to is not configured.
     */
    public void setGenerationAvailable(boolean generationAvailable) {
        this.generationAvailable = generationAvailable;
    }

    @Override
    public LlmDataQuery generate(LlmQueryGenerationRequest request) {
        return new LlmDataQuery(GENERATED_JPQL, List.of("orderNumber"), List.of(), "All order numbers",
                List.of(), null);
    }

    @Override
    public List<Map<String, Object>> execute(LlmQueryExecutionRequest request) {
        return List.of();
    }
}

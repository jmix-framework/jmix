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

import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmDataQueryService;
import io.jmix.reports.llm.LlmQueryExecutionRequest;
import io.jmix.reports.llm.LlmQueryGenerationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Stands in for the AI Tools-backed service: records what it was asked and returns preconfigured results,
 * so the loader can be tested without an LLM and without the add-on.
 */
public class TestLlmDataQueryService implements LlmDataQueryService {

    public static final String GENERATED_JPQL = "select o.number as orderNumber from sales_Order o";

    protected final List<LlmQueryGenerationRequest> generationRequests = new ArrayList<>();
    protected final List<LlmQueryExecutionRequest> executionRequests = new ArrayList<>();

    protected LlmDataQuery queryToGenerate = defaultGeneratedQuery();
    protected List<Map<String, Object>> rows = List.of(Map.of("orderNumber", "A-1"));

    @Override
    public LlmDataQuery generate(LlmQueryGenerationRequest request) {
        generationRequests.add(request);
        return queryToGenerate;
    }

    @Override
    public List<Map<String, Object>> execute(LlmQueryExecutionRequest request) {
        executionRequests.add(request);
        return rows;
    }

    public void reset() {
        generationRequests.clear();
        executionRequests.clear();
        queryToGenerate = defaultGeneratedQuery();
        rows = List.of(Map.of("orderNumber", "A-1"));
    }

    public List<LlmQueryGenerationRequest> getGenerationRequests() {
        return generationRequests;
    }

    public List<LlmQueryExecutionRequest> getExecutionRequests() {
        return executionRequests;
    }

    public LlmQueryGenerationRequest getLastGenerationRequest() {
        return generationRequests.get(generationRequests.size() - 1);
    }

    public LlmQueryExecutionRequest getLastExecutionRequest() {
        return executionRequests.get(executionRequests.size() - 1);
    }

    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows;
    }

    public void setQueryToGenerate(LlmDataQuery queryToGenerate) {
        this.queryToGenerate = queryToGenerate;
    }

    protected LlmDataQuery defaultGeneratedQuery() {
        return new LlmDataQuery(GENERATED_JPQL, List.of("orderNumber"), List.of(),
                "All order numbers", List.of(), null);
    }
}

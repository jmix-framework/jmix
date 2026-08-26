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
import io.jmix.reports.llm.LlmQueryGenerationRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Stands in for the AI Tools-backed service, recording what it was asked. A report run is supposed to ask it
 * nothing at all — the query it executes is the one stored with the report — and that is what these records let
 * a test state.
 */
public class TestLlmDataQueryService implements LlmDataQueryService {

    public static final String GENERATED_JPQL = "select o.number as orderNumber from sales_Order o";

    protected final List<LlmQueryGenerationRequest> generationRequests = new ArrayList<>();
    protected final List<LlmDataQuery> validatedQueries = new ArrayList<>();

    @Override
    public boolean isGenerationAvailable() {
        return true;
    }

    @Override
    public LlmDataQuery generate(LlmQueryGenerationRequest request) {
        generationRequests.add(request);
        return new LlmDataQuery(GENERATED_JPQL, List.of("orderNumber"), List.of(),
                "All order numbers", List.of());
    }

    @Override
    public List<String> validate(LlmDataQuery query) {
        validatedQueries.add(query);
        return List.of();
    }

    public List<LlmQueryGenerationRequest> getGenerationRequests() {
        return generationRequests;
    }

    public List<LlmDataQuery> getValidatedQueries() {
        return validatedQueries;
    }

    public void reset() {
        generationRequests.clear();
        validatedQueries.clear();
    }
}

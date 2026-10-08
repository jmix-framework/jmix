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

import io.jmix.reports.libintegration.LlmDataLoader;
import io.jmix.reports.llm.LlmDataQuery;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The loader with its one outward step replaced: instead of running the query it records what would have been run and
 * answers with preconfigured rows.
 */
public class TestLlmDataLoader extends LlmDataLoader {

    protected final List<Execution> executions = new ArrayList<>();

    protected List<Map<String, Object>> rows = List.of();

    protected int storeResolutions;

    @Nullable
    protected RuntimeException failure;

    /**
     * What one call of the query would have executed. {@code jpql} is the text as it would have run — the stored
     * one with the row-level policies of what it reads woven in, which for an entity without policies is the
     * stored text unchanged.
     */
    public record Execution(String jpql, List<String> resultProperties,
                            Map<String, @Nullable Object> arguments, String storeName) {
    }

    /**
     * Counts how often the store of a query had to be worked out, which the run remembers per query text.
     */
    @Override
    protected String resolveStoreName(LlmDataQuery query) {
        storeResolutions++;
        return super.resolveStoreName(query);
    }

    @Override
    protected List<Map<String, @Nullable Object>> executeQuery(LlmDataQuery query,
                                                               String jpql,
                                                               Map<String, @Nullable Object> arguments,
                                                               String storeName) {
        executions.add(new Execution(jpql, query.getResultProperties(), arguments, storeName));
        if (failure != null) {
            throw failure;
        }

        // Copied the way the real execution builds them: a band row is written into by the report engine.
        return rows.stream().map(row -> (Map<String, @Nullable Object>) new LinkedHashMap<>(row)).toList();
    }

    public Execution getLastExecution() {
        if (executions.isEmpty()) {
            throw new IllegalStateException("The query was not executed");
        }
        return executions.get(executions.size() - 1);
    }

    public List<Execution> getExecutions() {
        return executions;
    }

    public int getStoreResolutions() {
        return storeResolutions;
    }

    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows;
    }

    public void setFailure(@Nullable RuntimeException failure) {
        this.failure = failure;
    }

    public void reset() {
        executions.clear();
        rows = List.of();
        failure = null;
        storeResolutions = 0;
    }
}

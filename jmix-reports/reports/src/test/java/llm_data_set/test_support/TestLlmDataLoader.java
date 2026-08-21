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
import io.jmix.reports.llm.LlmQueryParameter;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The loader with its one outward step replaced: instead of running the query it records what would have been
 * run and answers with preconfigured rows.
 * <p>
 * Everything the loader decides — which query to read, which values to bind under which names, what to refuse —
 * is decided before that step, so this keeps those tests free of a database while still observing the decisions
 * where they are made. Execution against a real database is covered by the report-run tests.
 */
public class TestLlmDataLoader extends LlmDataLoader {

    protected final List<Execution> executions = new ArrayList<>();

    protected List<Map<String, Object>> rows = List.of();

    @Nullable
    protected RuntimeException failure;

    /**
     * What one call of the query would have executed.
     */
    public record Execution(String jpql, List<String> resultProperties, List<LlmQueryParameter> arguments) {
    }

    @Override
    protected List<Map<String, @Nullable Object>> executeQuery(LlmDataQuery query,
                                                               List<LlmQueryParameter> arguments) {
        executions.add(new Execution(query.getJpql(), query.getResultProperties(), arguments));
        if (failure != null) {
            throw failure;
        }
        return List.copyOf(rows);
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
    }
}

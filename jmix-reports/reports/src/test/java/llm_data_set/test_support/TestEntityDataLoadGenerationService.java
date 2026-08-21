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

import io.jmix.aitools.dataload.EntityDataLoadQuery;
import io.jmix.aitools.dataload.execution.GeneratedJpqlParameter;
import io.jmix.aitools.dataload.generation.EntityDataLoadGenerationService;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Records the text sent to query generation and returns a configurable draft, standing in for the LLM.
 */
public class TestEntityDataLoadGenerationService implements EntityDataLoadGenerationService {

    protected String lastUserText = "";

    protected String jpql = "select o.number as orderNumber from sales_Order o";
    protected List<GeneratedJpqlParameter> parameters = List.of();
    protected List<String> resultProperties = List.of("orderNumber");
    protected String explanation = "All order numbers";
    protected List<String> warnings = List.of();

    @Nullable
    protected RuntimeException failure;

    @Override
    public EntityDataLoadQuery generate(String userText) {
        lastUserText = userText;
        if (failure != null) {
            throw failure;
        }
        return new EntityDataLoadQuery(jpql, parameters, resultProperties, explanation, warnings, null, null);
    }

    public String getLastUserText() {
        return lastUserText;
    }

    public void setJpql(String jpql) {
        this.jpql = jpql;
    }

    public void setParameters(List<GeneratedJpqlParameter> parameters) {
        this.parameters = parameters;
    }

    public void setResultProperties(List<String> resultProperties) {
        this.resultProperties = resultProperties;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public void setFailure(RuntimeException failure) {
        this.failure = failure;
    }
}

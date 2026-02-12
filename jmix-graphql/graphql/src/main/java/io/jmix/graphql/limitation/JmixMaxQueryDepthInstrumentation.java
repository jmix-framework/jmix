/*
 * Copyright 2021 Haulmont.
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

package io.jmix.graphql.limitation;

import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.SimpleInstrumentationContext;
import graphql.execution.instrumentation.parameters.InstrumentationValidationParameters;
import graphql.validation.ValidationError;
import io.jmix.graphql.InstrumentationUtils;

import java.util.List;

public class JmixMaxQueryDepthInstrumentation extends MaxQueryDepthInstrumentation {

    private final int maxDepth;

    public JmixMaxQueryDepthInstrumentation(int maxDepth) {
        super(maxDepth);
        this.maxDepth = maxDepth;
    }

    @Override
    public InstrumentationContext<List<ValidationError>> beginValidation(InstrumentationValidationParameters parameters) {
        // skip validation for introspection query
        // also skip for maxDepth == 0
        if (InstrumentationUtils.isIntrospectionQuery(parameters.getExecutionInput()) || maxDepth == 0) {
            return new SimpleInstrumentationContext<>();
        }

        return super.beginValidation(parameters);
    }
}

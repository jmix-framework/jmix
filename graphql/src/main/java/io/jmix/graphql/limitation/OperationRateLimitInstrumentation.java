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

import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationValidationParameters;
import graphql.validation.ValidationError;
import io.jmix.graphql.datafetcher.EnvironmentUtils;

import java.util.List;

import static graphql.execution.instrumentation.SimpleInstrumentationContext.whenCompleted;

public class OperationRateLimitInstrumentation extends SimpleInstrumentation {

    private final OperationRateLimitService operationRateLimitService;

    public OperationRateLimitInstrumentation(OperationRateLimitService operationRateLimitService) {
        this.operationRateLimitService = operationRateLimitService;
    }

    @Override
    public  InstrumentationContext<List<ValidationError>> beginValidation(InstrumentationValidationParameters parameters) {
        return whenCompleted((errors, throwable) -> {
            if (operationRateLimitService.isRateLimited()) {
                operationRateLimitService.queryPerformed(EnvironmentUtils.getRemoteIPAddress(parameters.getContext()));
            }
        });
    }
}

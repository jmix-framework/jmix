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

package io.jmix.aitools.dataload.execution;

import io.jmix.aitools.dataload.repair.JpqlRepairResult;
import io.jmix.aitools.dataload.repair.JpqlRepairService;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import io.jmix.aitools.dataload.validation.JpqlValidationService;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("aitols_JpqlValidationAndRepairService")
public class JpqlValidationAndRepairService {

    @Autowired
    protected JpqlRepairService jpqlRepairService;

    @Autowired
    protected JpqlValidationService jpqlValidationService;

    protected OperationResult validateAndRepair(JpqlExecutionRequest request) {
        if (request.getResultProperties().isEmpty()) {
            JpqlValidationResult validationResult = new JpqlValidationResult(false, List.of(
                    new JpqlValidationIssue("resultProperties.empty",
                            "resultProperties must be specified for loadValues execution")
            ));
            return OperationResult.failed(request, toGeneratedJpqlResult(request), validationResult, null);
        }

        GeneratedJpqlResult initialGeneratedResult = toGeneratedJpqlResult(request);

        // Validate LLM generated JPQL
        JpqlValidationResult initialValidationResult = jpqlValidationService.validate(initialGeneratedResult);

        // Repair it if needed
        JpqlRepairResult repairResult = jpqlRepairService.repairIfNeeded(request, initialGeneratedResult, initialValidationResult);
        GeneratedJpqlResult generatedResult = repairResult.getGeneratedJpqlResult();

        // Final validation of repaired result
        JpqlValidationResult validationResult = jpqlValidationService.validate(generatedResult);

        if (!validationResult.isValid()) {
            return OperationResult.failed(request, generatedResult, validationResult, repairResult);
        }

        return OperationResult.success(request, generatedResult, validationResult, repairResult);
    }

    protected GeneratedJpqlResult toGeneratedJpqlResult(JpqlExecutionRequest request) {
        return new GeneratedJpqlResult(request.getJpql(), toGeneratedParameters(request.getParameters()),
                "", List.of(), request.getMaxResults(), request.getFirstResult()
        );
    }

    protected List<GeneratedJpqlParameter> toGeneratedParameters(List<JpqlExecutionParameter> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return List.of();
        }

        List<GeneratedJpqlParameter> generatedParameters = new ArrayList<>(parameters.size());
        for (JpqlExecutionParameter parameter : parameters) {
            generatedParameters.add(
                    new GeneratedJpqlParameter(parameter.getName(), parameter.getType(), parameter.getValue()));
        }
        return List.copyOf(generatedParameters);
    }

    public static class OperationResult {

        protected JpqlExecutionRequest request;
        protected JpqlRepairResult repairResult;
        protected GeneratedJpqlResult generatedResult;
        protected JpqlValidationResult validationResult;

        protected boolean failed;

        protected OperationResult(JpqlExecutionRequest request,
                                  GeneratedJpqlResult generatedResult,
                                  JpqlValidationResult validationResult,
                                  @Nullable JpqlRepairResult repairResult, boolean failed) {
            this.request = request;
            this.generatedResult = generatedResult;
            this.validationResult = validationResult;
            this.repairResult = repairResult;
            this.failed = failed;
        }

        public static OperationResult success(JpqlExecutionRequest request,
                                              GeneratedJpqlResult generatedResult,
                                              JpqlValidationResult validationResult,
                                              @Nullable JpqlRepairResult repairResult) {
            return new OperationResult(request, generatedResult, validationResult, repairResult, false);
        }

        public static OperationResult failed(JpqlExecutionRequest request,
                                             GeneratedJpqlResult generatedResult,
                                             JpqlValidationResult validationResult,
                                             @Nullable JpqlRepairResult repairResult) {
            return new OperationResult(request, generatedResult, validationResult, repairResult, true);
        }

        public boolean isFailed() {
            return failed;
        }

        public JpqlExecutionRequest getRequest() {
            return request;
        }

        public GeneratedJpqlResult getGeneratedResult() {
            return generatedResult;
        }

        public JpqlValidationResult getValidationResult() {
            return validationResult;
        }

        @Nullable
        public JpqlRepairResult getRepairResult() {
            return repairResult;
        }

        public boolean isRepaired() {
            return repairResult != null && repairResult.isRepaired();
        }
    }
}

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
import io.jmix.aitools.dataload.validation.validator.JpqlValidatorSupport;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates a generated JPQL query and repairs it when validation fails.
 * <p>
 * The query is validated, then repaired if needed and validated again. The combined outcome —
 * the (possibly repaired) query, the final validation result and the repair details — is returned
 * as an {@link OperationResult}, which also reports whether the operation ultimately failed.
 * The rows of the final query are keyed by the requested result properties, so the outcome fails when the
 * requested names do not match the select clause, or when a repair dropped, added or moved a selected value.
 */
@NullMarked
@Component("aitls_JpqlValidationAndRepairService")
public class JpqlValidationAndRepairService {

    public static final String RESULT_PROPERTIES_MISMATCH_CODE = "resultProperties.mismatch";
    public static final String RESULT_PROPERTIES_UNREADABLE_CODE = "resultProperties.unreadable";

    protected static final String RESULT_PROPERTIES_GUIDANCE = "Alias every selected value with AS and list the"
            + " aliases in resultProperties, in select-clause order.";
    protected static final String REPAIRED_QUERY_GUIDANCE = "Call the tool again with jpql set to the repaired query"
            + " returned in generatedJpqlResult.jpql and resultProperties set to %s, in select-clause order.";
    protected static final String REPAIRED_QUERY_UNREADABLE_GUIDANCE = "Call the tool again with jpql set to the"
            + " repaired query returned in generatedJpqlResult.jpql, alias every selected value with AS, and list"
            + " the aliases in resultProperties, in select-clause order.";

    @Autowired
    protected JpqlRepairService jpqlRepairService;

    @Autowired
    protected JpqlValidationService jpqlValidationService;

    /**
     * Validates the query from the request and repairs it if the first validation fails.
     *
     * @param request execution request carrying the query, its parameters and result properties
     * @return outcome with the final query
     */
    public OperationResult validateAndRepair(JpqlExecutionRequest request) {
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

        if (repairResult.isRepaired()) {
            // The repair answers with a query text alone, and the rows stay keyed by the requested names, so the
            // repaired query must keep the requested columns in place.
            JpqlValidationIssue issue = checkRepairedColumns(request.getResultProperties(), request.getJpql(),
                    generatedResult.getJpql());
            if (issue != null) {
                return OperationResult.failed(request, generatedResult, failedWith(validationResult, issue),
                        repairResult);
            }
            return OperationResult.success(request, generatedResult, validationResult, repairResult);
        }

        JpqlValidationIssue mismatch = checkResultProperties(request.getResultProperties(), generatedResult.getJpql());
        if (mismatch != null) {
            return OperationResult.failed(request, generatedResult, failedWith(validationResult, mismatch),
                    repairResult);
        }

        return OperationResult.success(request, generatedResult, validationResult, repairResult);
    }

    /**
     * Checks that the requested column names describe the query's columns: one name per selected value and, at
     * every position where the value is aliased, the alias itself (ignoring case). A value without an alias
     * accepts any name.
     *
     * @param resultProperties requested column names
     * @param jpql             validated query
     * @return the mismatch issue, or {@code null} if the names match the query
     */
    @Nullable
    protected JpqlValidationIssue checkResultProperties(List<String> resultProperties, String jpql) {
        List<@Nullable String> aliases = JpqlValidatorSupport.selectedAliases(jpql);
        if (resultProperties.size() != aliases.size()) {
            return new JpqlValidationIssue(RESULT_PROPERTIES_MISMATCH_CODE,
                    "resultProperties list " + resultProperties.size() + " names, but the query selects "
                            + aliases.size() + " values",
                    RESULT_PROPERTIES_GUIDANCE);
        }

        for (int i = 0; i < aliases.size(); i++) {
            String alias = aliases.get(i);
            if (alias != null && !alias.equalsIgnoreCase(resultProperties.get(i))) {
                return new JpqlValidationIssue(RESULT_PROPERTIES_MISMATCH_CODE,
                        "resultProperties " + resultProperties + " do not match the select aliases " + aliases
                                + ": position " + (i + 1) + " is aliased " + alias + " but named "
                                + resultProperties.get(i),
                        RESULT_PROPERTIES_GUIDANCE);
            }
        }
        return null;
    }

    /**
     * Checks that a repaired query still returns the requested columns, position by position. The rows are keyed
     * by the requested names, so at every position the repair may rename the alias (as it does with a reserved
     * word) or correct the expression (as it does with a misspelled attribute), but not both at once, and it may
     * not drop, add or move a column: the value would land under a name that means something else.
     *
     * @param resultProperties requested column names
     * @param originalJpql     query as requested, before the repair
     * @param repairedJpql     repaired query
     * @return the issue that refuses the repair, or {@code null} if the repaired query keeps the columns
     */
    @Nullable
    protected JpqlValidationIssue checkRepairedColumns(List<String> resultProperties, String originalJpql,
                                                       String repairedJpql) {
        List<@Nullable String> aliases = JpqlValidatorSupport.selectedAliases(repairedJpql);
        if (aliases.contains(null)) {
            return new JpqlValidationIssue(RESULT_PROPERTIES_UNREADABLE_CODE,
                    "The repaired query does not alias every selected value, so its columns cannot be checked",
                    REPAIRED_QUERY_UNREADABLE_GUIDANCE);
        }
        String guidance = String.format(REPAIRED_QUERY_GUIDANCE, aliases);
        if (aliases.size() != resultProperties.size()) {
            return new JpqlValidationIssue(RESULT_PROPERTIES_MISMATCH_CODE,
                    "The repaired query selects " + aliases.size() + " values " + aliases + ", but "
                            + resultProperties.size() + " columns were requested " + resultProperties,
                    guidance);
        }

        List<String> originalExpressions = JpqlValidatorSupport.selectedExpressions(originalJpql);
        List<String> repairedExpressions = JpqlValidatorSupport.selectedExpressions(repairedJpql);
        for (int i = 0; i < aliases.size(); i++) {
            String name = resultProperties.get(i);
            int aliasPosition = indexOfIgnoreCase(aliases, name);
            if (aliasPosition >= 0 && aliasPosition != i) {
                return new JpqlValidationIssue(RESULT_PROPERTIES_MISMATCH_CODE,
                        "The repaired query moved the column " + name + " from position " + (i + 1)
                                + " to position " + (aliasPosition + 1) + "; its aliases are " + aliases
                                + ", the requested columns " + resultProperties,
                        guidance);
            }
            boolean aliasKept = aliasPosition == i;
            boolean expressionKept = i < originalExpressions.size()
                    && originalExpressions.get(i).equals(repairedExpressions.get(i));
            if (!aliasKept && !expressionKept) {
                return new JpqlValidationIssue(RESULT_PROPERTIES_MISMATCH_CODE,
                        "The repaired query changed both the value and the alias at position " + (i + 1)
                                + " (" + repairedExpressions.get(i) + " as " + aliases.get(i) + "), so it is not the"
                                + " requested column " + name,
                        guidance);
            }
        }
        return null;
    }

    protected int indexOfIgnoreCase(List<@Nullable String> aliases, String name) {
        for (int i = 0; i < aliases.size(); i++) {
            if (name.equalsIgnoreCase(aliases.get(i))) {
                return i;
            }
        }
        return -1;
    }

    protected JpqlValidationResult failedWith(JpqlValidationResult validationResult, JpqlValidationIssue issue) {
        List<JpqlValidationIssue> issues = new ArrayList<>(validationResult.getIssues());
        issues.add(issue);
        return new JpqlValidationResult(false, issues);
    }

    protected GeneratedJpqlResult toGeneratedJpqlResult(JpqlExecutionRequest request) {
        return new GeneratedJpqlResult(request.getJpql(), toGeneratedParameters(request.getParameters()),
                "", List.of(), request.getMaxResults(), request.getFirstResult()
        );
    }

    protected List<GeneratedJpqlParameter> toGeneratedParameters(@Nullable List<JpqlExecutionParameter> parameters) {
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

    /**
     * Combined outcome of the validate-and-repair operation.
     */
    public static class OperationResult {

        protected JpqlExecutionRequest request;
        @Nullable
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

        /**
         * Creates a successful outcome.
         *
         * @param request          original execution request
         * @param generatedResult  final query draft after validation and any repair
         * @param validationResult final validation result
         * @param repairResult     repair details, or {@code null} if no repair was attempted
         * @return successful outcome
         */
        public static OperationResult success(JpqlExecutionRequest request,
                                              GeneratedJpqlResult generatedResult,
                                              JpqlValidationResult validationResult,
                                              @Nullable JpqlRepairResult repairResult) {
            return new OperationResult(request, generatedResult, validationResult, repairResult, false);
        }

        /**
         * Creates a failed outcome.
         *
         * @param request          original execution request
         * @param generatedResult  final query draft after validation and any repair
         * @param validationResult final validation result
         * @param repairResult     repair details, or {@code null} if no repair was attempted
         * @return failed outcome
         */
        public static OperationResult failed(JpqlExecutionRequest request,
                                             GeneratedJpqlResult generatedResult,
                                             JpqlValidationResult validationResult,
                                             @Nullable JpqlRepairResult repairResult) {
            return new OperationResult(request, generatedResult, validationResult, repairResult, true);
        }

        /**
         * Returns whether validation failed and the query must not be executed.
         *
         * @return {@code true} if the operation failed
         */
        public boolean isFailed() {
            return failed;
        }

        /**
         * Returns the original execution request.
         *
         * @return execution request
         */
        public JpqlExecutionRequest getRequest() {
            return request;
        }

        /**
         * Returns the final query draft after validation and any repair.
         *
         * @return final query draft
         */
        public GeneratedJpqlResult getGeneratedResult() {
            return generatedResult;
        }

        /**
         * Returns the final validation result.
         *
         * @return validation result
         */
        public JpqlValidationResult getValidationResult() {
            return validationResult;
        }

        /**
         * Returns the repair details.
         *
         * @return repair details, or {@code null} if no repair was attempted
         */
        @Nullable
        public JpqlRepairResult getRepairResult() {
            return repairResult;
        }

        /**
         * Returns whether the query was actually repaired.
         *
         * @return {@code true} if the query was repaired
         */
        public boolean isRepaired() {
            return repairResult != null && repairResult.isRepaired();
        }
    }
}

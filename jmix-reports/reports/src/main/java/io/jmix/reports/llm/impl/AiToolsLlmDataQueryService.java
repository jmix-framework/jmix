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

package io.jmix.reports.llm.impl;

import io.jmix.aitools.ChatClientFactory;
import io.jmix.aitools.dataload.EntityDataLoadQuery;
import io.jmix.aitools.dataload.execution.*;
import io.jmix.aitools.dataload.generation.EntityDataLoadUserMessageComposer;
import io.jmix.aitools.dataload.generation.EntityDataLoadGenerationRequest;
import io.jmix.aitools.dataload.generation.EntityDataLoadGenerationService;
import io.jmix.aitools.dataload.generation.EntityDataLoadQueryParameter;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import io.jmix.aitools.dataload.validation.JpqlValidationResult;
import io.jmix.aitools.dataload.validation.JpqlValidationService;
import io.jmix.aitools.dataload.validation.validator.JpqlValidatorSupport;
import io.jmix.reports.llm.*;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implements the Reports-side seam on top of the AI Tools data-load subsystem: it turns a prompt into a query,
 * gives a faulty generated query one chance to be corrected, and checks a query — all of which happen while a
 * report is authored in the designer.
 * <p>
 * The only class in Reports that depends on the AI Tools add-on, and therefore the only one that must not
 * be loaded when the add-on is absent — its bean is declared by a conditional auto-configuration.
 */
public class AiToolsLlmDataQueryService implements LlmDataQueryService {

    private static final Logger log = LoggerFactory.getLogger(AiToolsLlmDataQueryService.class);

    @Autowired
    protected EntityDataLoadGenerationService entityDataLoadGenerationService;

    @Autowired
    protected JpqlValidationAndRepairService jpqlValidationAndRepairService;

    @Autowired
    protected JpqlValidationService jpqlValidationService;

    @Autowired
    protected ChatClientFactory chatClientFactory;

    @Autowired
    protected EntityDataLoadUserMessageComposer userMessageComposer;

    /**
     * The add-on's data-load beans are declared on properties alone, so they are there even when no model is
     * configured for the application. Generation then fails on the first call, which the designer has no reason
     * to find out by trying.
     */
    @Override
    public boolean isGenerationAvailable() {
        return chatClientFactory.isConfigured();
    }

    @Override
    public LlmDataQuery generate(LlmQueryGenerationRequest request) {
        EntityDataLoadGenerationRequest generationRequest = toGenerationRequest(request);

        EntityDataLoadQuery generatedQuery;
        try {
            generatedQuery = entityDataLoadGenerationService.generate(generationRequest);
        } catch (RuntimeException e) {
            throw new LlmDataQueryException("Cannot generate a query for the data set prompt", e);
        }

        if (StringUtils.isBlank(generatedQuery.getJpql())) {
            throw new LlmDataQueryException("Query generation produced no query text");
        }

        // A model answers with the lists it likes, and nothing between here and the model rejects a null
        // element in them, so they are cleaned before the query is built out of them.
        // The row count belongs to the answer, not to the text: the add-on's contract puts "the top 5 customers"
        // into maxResults and forbids `limit` inside JPQL, which JPQL has no place for anyway.
        LlmDataQuery query = new LlmDataQuery(generatedQuery.getJpql(),
                retainNonNull(generatedQuery.getResultProperties()),
                toQueryParameters(generatedQuery.getJpql(), generatedQuery.getParameters()),
                generatedQuery.getExplanation(), retainNonNull(generatedQuery.getWarnings()),
                generatedQuery.getMaxResults(), generatedQuery.getFirstResult());

        // Repair gets the same message generation was sent (prompt + constraints), not just the prompt —
        // otherwise it wouldn't know the required aliases or optional guards and could undo them.
        String userText = userMessageComposer.compose(generationRequest);
        return repairIfNeeded(userText, query);
    }

    /**
     * Turns the seam request into the add-on's structured generation request: the parameters and their binding
     * flags become data, and the only prose Reports still owns — the cross-tab narrowing — goes into the prompt
     * the add-on treats as free text.
     *
     * @param request the seam request
     * @return the add-on generation request carrying the mapped parameters, the required result columns and the
     *         prompt with any cross-tab intent appended
     */
    protected EntityDataLoadGenerationRequest toGenerationRequest(LlmQueryGenerationRequest request) {
        List<EntityDataLoadQueryParameter> parameters = request.getAvailableParameters().stream()
                .map(parameter -> new EntityDataLoadQueryParameter(parameter.getName(), parameter.getJavaType())
                        .setMultiValued(parameter.isMultiValued())
                        .setOptional(parameter.isOptional()))
                .toList();

        return new EntityDataLoadGenerationRequest(composePrompt(request))
                .setAvailableParameters(parameters)
                .setRequiredResultProperties(request.getRequiredResultProperties());
    }

    /**
     * Appends the cross-tab intent to the prompt when the band is one: the required result columns are its axes,
     * and each row must carry its axis values under those exact names. When an axis parameter is multi-valued,
     * the query is narrowed to the matrix by matching it with {@code IN}. This is Reports' own framing, so it
     * stays here rather than in the neutral add-on request.
     *
     * @param request the seam request
     * @return the prompt, with the cross-tab intent appended when there are required result columns
     */
    protected String composePrompt(LlmQueryGenerationRequest request) {
        List<String> requiredColumns = request.getRequiredResultProperties();
        if (requiredColumns.isEmpty()) {
            return request.getPrompt();
        }

        StringBuilder prompt = new StringBuilder(request.getPrompt());
        prompt.append("\n\nThis band is a cross-tab: the required result columns are its axes, so each row ")
                .append("must carry its axis values under those exact column names.");

        boolean hasMultiValuedAxisParameter = request.getAvailableParameters().stream()
                .filter(LlmQueryParameter::isMultiValued)
                .map(LlmQueryParameter::getName)
                .anyMatch(requiredColumns::contains);

        if (hasMultiValuedAxisParameter) {
            prompt.append(" When a required column is also a multi-valued available parameter, narrow the query ")
                    .append("to the matrix by matching that parameter with IN (no parentheses around the name).");
        }

        return prompt.toString();
    }

    /**
     * Gives a query the add-on found faulty one chance to be corrected, which is what the add-on's own
     * generation path does. Repair belongs to generation alone: a query the author then edits by hand is theirs,
     * and a report run has no model to ask.
     * <p>
     * Best effort — a query that is still faulty afterwards, and a repair that could not be carried out at all,
     * are both answered with a query rather than with a failure: the designer says what is wrong with it, and an
     * author can correct by hand what a model could not.
     *
     * @param userText the request the query was generated from, which repair is told to satisfy
     * @param query    the generated query
     * @return the repaired query, or the generated one when repair did not happen or did not help
     */
    protected LlmDataQuery repairIfNeeded(String userText, LlmDataQuery query) {
        JpqlValidationAndRepairService.OperationResult outcome;
        try {
            outcome = jpqlValidationAndRepairService.validateAndRepair(toExecutionRequest(userText, query));
        } catch (RuntimeException e) {
            log.warn("Cannot repair the generated query, keeping it as generated", e);
            return query;
        }

        if (!outcome.isRepaired()) {
            return query;
        }

        GeneratedJpqlResult repaired = outcome.getGeneratedResult();
        if (StringUtils.isBlank(repaired.getJpql())) {
            log.warn("Repair of the generated query produced no query text, keeping it as generated");
            return query;
        }

        List<String> resultProperties = resultPropertiesOf(repaired.getJpql());
        if (resultProperties == null) {
            // Its columns cannot be read, and the generated ones do not describe this text: keeping the query
            // as generated is the only answer that leaves nothing mismatched.
            return query;
        }

        log.debug("The generated query was repaired: {}", repaired.getJpql());
        // Repair answers about the text; how many rows the prompt asked for is unchanged by it.
        return new LlmDataQuery(repaired.getJpql(), resultProperties,
                toQueryParameters(repaired.getJpql(), repaired.getParameters()),
                StringUtils.defaultIfBlank(repaired.getExplanation(), query.getExplanation()),
                retainNonNull(repaired.getWarnings()), query.getMaxResults(), query.getFirstResult());
    }

    protected JpqlExecutionRequest toExecutionRequest(String userText, LlmDataQuery query) {
        List<JpqlExecutionParameter> parameters = new ArrayList<>(query.getParameters().size());
        for (LlmQueryParameter parameter : query.getParameters()) {
            parameters.add(new JpqlExecutionParameter(parameter.getName(), parameter.getJavaType(), null));
        }

        // No paging: the report executes the query itself and takes every row the query returns.
        return new JpqlExecutionRequest(userText, query.getJpql(), parameters, query.getResultProperties(),
                null, null);
    }

    /**
     * Reads the columns of a repaired query off its select clause. Repair answers with a query text alone —
     * the add-on carries the columns beside it, unchanged — while a repair can well rename them, which is
     * what it does with an alias that turned out to be a reserved word.
     * <p>
     * Columns are handed to the query by position, so a list naming fewer values than the query selects would
     * put values under the wrong names, silently. A repair is asked to alias every selected value; when it did
     * not, its columns are unreadable and the repair is not used at all — pairing its text with the columns of
     * the generated query would be the same mismatch by another route.
     *
     * @param jpql the repaired query
     * @return the columns the repaired query returns in select-clause order, or {@code null} when they cannot
     *         be read from it
     */
    @Nullable
    protected List<String> resultPropertiesOf(String jpql) {
        String selectClause = selectClauseOf(jpql);
        List<String> aliases = JpqlValidatorSupport.extractAliases(selectClause);
        if (aliases.isEmpty()) {
            log.warn("The repaired query names none of its selected values, so it is not used");
            return null;
        }

        int selected = selectedExpressionCount(selectClause);
        if (aliases.size() != selected) {
            log.warn("The repaired query names {} of its {} selected values, so it is not used",
                    aliases.size(), selected);
            return null;
        }

        return aliases;
    }

    /**
     * Counts the values a select clause selects: its top-level commas plus one. A comma inside a function call
     * or a subquery separates arguments rather than selected values, and a comma inside a string literal is
     * text — the clause arrives with its literals already blanked by {@link #selectClauseOf}.
     */
    protected int selectedExpressionCount(String selectClause) {
        int depth = 0;
        int count = 1;
        for (int i = 0; i < selectClause.length(); i++) {
            char character = selectClause.charAt(i);
            if (character == '(') {
                depth++;
            } else if (character == ')') {
                depth--;
            } else if (character == ',' && depth == 0) {
                count++;
            }
        }

        return count;
    }

    /**
     * Returns the part of the query before its own {@code from}, which is where the columns are named. A
     * {@code from} of a subquery is inside parentheses, so only the depth-zero one ends the select clause.
     */
    protected String selectClauseOf(String jpql) {
        String text = JpqlValidatorSupport.stripStringLiterals(jpql);
        int depth = 0;
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (character == '(') {
                depth++;
            } else if (character == ')') {
                depth--;
            } else if (depth == 0 && text.regionMatches(true, i, "from", 0, 4)
                    && isWordBoundary(text, i - 1) && isWordBoundary(text, i + 4)) {
                return text.substring(0, i);
            }
        }

        return text;
    }

    /**
     * Whether the character at this index ends a word. An underscore does not: an alias like {@code valid_from}
     * carries the letters {@code from} inside it, and taking that for the query's own {@code from} would cut the
     * select clause in the middle of a name.
     */
    protected boolean isWordBoundary(String text, int index) {
        if (index < 0 || index >= text.length()) {
            return true;
        }

        char character = text.charAt(index);
        return !Character.isLetterOrDigit(character) && character != '_';
    }

    @Override
    public List<String> validate(LlmDataQuery query) {
        JpqlValidationResult validationResult = jpqlValidationService.validate(toGeneratedResult(query));
        if (validationResult.isValid()) {
            return List.of();
        }

        return validationResult.getIssues().stream()
                .map(JpqlValidationIssue::getMessage)
                .toList();
    }

    protected <T> List<T> retainNonNull(@Nullable List<T> values) {
        if (values == null) {
            return List.of();
        }

        //noinspection ConstantValue
        return values.stream()
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Declares the parameters the generated text references, and nothing else. The model's own list only
     * supplies the Java types: a list that says anything else than the text does is what the add-on rejects a
     * query for, and a query stored with such a list fails on every run instead of once here.
     * <p>
     * The values are dropped along the way: the values a model invents belong to the request it was generated
     * for, while the query is stored and reused.
     */
    protected List<LlmQueryParameter> toQueryParameters(String jpql,
                                                        List<GeneratedJpqlParameter> generatedParameters) {
        Map<String, String> declaredTypes = new LinkedHashMap<>();
        for (GeneratedJpqlParameter generatedParameter : generatedParameters) {
            if (StringUtils.isNotBlank(generatedParameter.getName())) {
                declaredTypes.put(generatedParameter.getName(),
                        StringUtils.defaultString(generatedParameter.getType()));
            }
        }

        List<LlmQueryParameter> parameters = new ArrayList<>();
        for (String name : JpqlValidatorSupport.referencedParameters(jpql)) {
            parameters.add(new LlmQueryParameter(name, declaredTypes.getOrDefault(name, "")));
        }
        return parameters;
    }

    /**
     * Describes a query the way the add-on's validation reads one. The arguments are not part of it: a check
     * is about the query alone, while the values it is run with are decided per run.
     */
    protected GeneratedJpqlResult toGeneratedResult(LlmDataQuery query) {
        List<GeneratedJpqlParameter> parameters = new ArrayList<>(query.getParameters().size());
        for (LlmQueryParameter parameter : query.getParameters()) {
            parameters.add(new GeneratedJpqlParameter(parameter.getName(), parameter.getJavaType(), null));
        }

        return new GeneratedJpqlResult(query.getJpql(), parameters, StringUtils.defaultString(query.getExplanation()),
                query.getWarnings());
    }

}

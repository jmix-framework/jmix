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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmDataQueryException;
import io.jmix.reports.llm.LlmQueryParameter;
import io.jmix.reports.llm.LlmQueryParameterNames;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Reads and writes the document a generated query is stored as in {@link DataSet#getLlmGeneratedQuery()}.
 * <p>
 * Parameter values are never written: {@link LlmQueryParameter} keeps its value transient, so one stored
 * query stays valid for any arguments.
 */
@Component("report_LlmDataQuerySerializer")
public class LlmDataQuerySerializer {

    protected static final String REGENERATE_HINT = "the stored query is unreadable, regenerate it";

    /**
     * HTML escaping is off: a query is full of {@code >}, {@code <} and {@code =}, which Gson escapes by
     * default, writing them as unicode escapes into the document the report XML carries. Nothing reads them
     * that way except the people looking at an exported report or at its diff.
     */
    protected final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    /**
     * @param query query to store
     * @return the query as a JSON document
     */
    public String toJson(LlmDataQuery query) {
        return gson.toJson(query);
    }

    /**
     * Reads a stored query back.
     *
     * @param json stored document, or {@code null}
     * @return the query, or {@code null} if nothing is stored yet
     * @throws LlmDataQueryException if the document cannot be read or carries no query text
     */
    @Nullable
    public LlmDataQuery fromJson(@Nullable String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }

        LlmDataQuery parsed;
        try {
            parsed = gson.fromJson(json, LlmDataQuery.class);
        } catch (JsonParseException e) {
            throw new LlmDataQueryException(REGENERATE_HINT, e);
        }

        // Gson instantiates behind the constructor and honours no nullness contract: a "null" document parses to
        // null and a stored collection may stay null, so the checks here are reachable despite the annotations.
        //noinspection ConstantValue
        if (parsed == null || StringUtils.isBlank(parsed.getJpql())) {
            throw new LlmDataQueryException(REGENERATE_HINT);
        }

        // Rebuild through the constructor to restore the invariants Gson skipped. A stored JSON array may
        // hold nulls, which the query rejects, so they are dropped here.
        return new LlmDataQuery(
                parsed.getJpql(),
                retainNonNull(parsed.getResultProperties()),
                retainNamedParameters(parsed.getParameters()),
                parsed.getExplanation(),
                retainNonNull(parsed.getWarnings()));
    }

    protected <T> List<T> retainNonNull(@Nullable List<T> values) {
        // A stored JSON array may hold nulls, whatever the element type claims.
        //noinspection ConstantValue
        return values == null ? Collections.emptyList() : values.stream().filter(Objects::nonNull).toList();
    }

    /**
     * Keeps the parameters that can actually be bound — a stored parameter without a name is useless — and gives
     * one whose type the document omitted an empty type rather than none, which the constructor rejects.
     */
    protected List<LlmQueryParameter> retainNamedParameters(@Nullable List<LlmQueryParameter> parameters) {
        return retainNonNull(parameters).stream()
                .filter(parameter -> StringUtils.isNotBlank(parameter.getName()))
                .map(parameter -> new LlmQueryParameter(parameter.getName(),
                        StringUtils.defaultString(parameter.getJavaType())))
                .toList();
    }

    /**
     * Assembles the document of a query edited by hand out of its text and its columns.
     * <p>
     * The parameters are not edited but re-derived from the text the way the add-on validates queries by
     * ({@link LlmQueryParameterNames#referencedIn}), so a hand-written {@code :name} is declared by the act of
     * writing it and a removed one disappears;
     * a parameter the previous document declared keeps its Java type, and one that appears with the edit has
     * none to keep — the loader types every argument from the run's own dictionary anyway.
     * The explanation and the warnings of the query being replaced are carried over: they describe what the
     * model produced, and an edit does not make them false, only incomplete.
     *
     * @param jpql             query text as edited
     * @param resultProperties column names in select-clause order
     * @param previous         query being replaced, or {@code null} if there is none
     * @return the assembled query, ready to be stored with {@link #toJson(LlmDataQuery)}
     */
    public LlmDataQuery assemble(String jpql, List<String> resultProperties, @Nullable LlmDataQuery previous) {
        Map<String, String> typesOfPrevious = new LinkedHashMap<>();
        if (previous != null) {
            for (LlmQueryParameter parameter : previous.getParameters()) {
                typesOfPrevious.put(parameter.getName(), parameter.getJavaType());
            }
        }

        List<LlmQueryParameter> parameters = new ArrayList<>();
        for (String name : LlmQueryParameterNames.referencedIn(jpql)) {
            parameters.add(new LlmQueryParameter(name, typesOfPrevious.getOrDefault(name, "")));
        }

        return new LlmDataQuery(jpql, resultProperties, parameters,
                previous != null ? previous.getExplanation() : null,
                previous != null ? previous.getWarnings() : Collections.emptyList());
    }
}

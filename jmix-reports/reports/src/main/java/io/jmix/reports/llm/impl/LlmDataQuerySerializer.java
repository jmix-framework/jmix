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
import com.google.gson.JsonParseException;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmDataQueryException;
import io.jmix.reports.llm.LlmQueryParameter;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;
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

    protected final Gson gson = new Gson();

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
                retainNonNull(parsed.getWarnings()),
                parsed.getMaxResults());
    }

    protected <T> List<T> retainNonNull(@Nullable List<T> values) {
        // A stored JSON array may hold nulls, whatever the element type claims.
        //noinspection ConstantValue
        return values == null ? List.of() : values.stream().filter(Objects::nonNull).toList();
    }

    /**
     * Keeps the parameters that can actually be bound: a stored parameter without a name is useless.
     */
    protected List<LlmQueryParameter> retainNamedParameters(@Nullable List<LlmQueryParameter> parameters) {
        return retainNonNull(parameters).stream()
                .filter(parameter -> StringUtils.isNotBlank(parameter.getName()))
                .map(parameter -> new LlmQueryParameter(parameter.getName(),
                        StringUtils.defaultString(parameter.getJavaType()), null))
                .toList();
    }
}

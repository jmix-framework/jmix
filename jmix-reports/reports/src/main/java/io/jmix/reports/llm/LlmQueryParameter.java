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

package io.jmix.reports.llm;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * A named parameter of an {@link LlmDataQuery}: the name used in the JPQL text and the Java type of the value it
 * stands for. A parameter declares itself; it does not carry a value. The value a run binds comes from the run
 * — the report parameters, the rows of the parent bands, the cross-tab axes — which is what lets one stored
 * query serve every run of the report.
 * <p>
 * The type is what a model is told the value will be, and what the stored query keeps saying about it; a run
 * binds the value it holds as it is, without consulting the declared type. The same class describes a parameter
 * offered to query generation and one declared by a stored query.
 */
public class LlmQueryParameter {

    protected String name;
    protected String javaType;

    /**
     * Transient on purpose: it describes what the run offers, not what the stored query declares, and must not
     * become part of the stored document.
     */
    protected transient boolean multiValued;

    /**
     * Transient for the same reason as {@link #multiValued}: whether a run may leave this parameter empty is a
     * fact about the report, not about the query text that references it.
     */
    protected transient boolean optional;

    public LlmQueryParameter(String name, String javaType) {
        this(name, javaType, false, false);
    }

    public LlmQueryParameter(String name, String javaType, boolean multiValued) {
        this(name, javaType, multiValued, false);
    }

    public LlmQueryParameter(String name, String javaType, boolean multiValued, boolean optional) {
        checkNotNullArgument(name, "name is null");
        checkNotNullArgument(javaType, "javaType is null");

        this.name = name;
        this.javaType = javaType;
        this.multiValued = multiValued;
        this.optional = optional;
    }

    /**
     * @return parameter name as used in the JPQL text, without the leading colon
     */
    public String getName() {
        return name;
    }

    /**
     * @return fully qualified Java type name the value is bound as, for example {@code java.time.LocalDate}
     */
    public String getJavaType() {
        return javaType;
    }

    /**
     * Tells a parameter that carries several values of {@link #getJavaType()} from an ordinary single-valued
     * one, so that a query can match it with {@code IN}. This may be a collection-valued report parameter or
     * the values of one cross-tab axis. Stated by whoever offers the parameter, since a parameter carries no
     * value to infer it from.
     *
     * @return {@code true} if the value is a collection of {@link #getJavaType()}
     */
    public boolean isMultiValued() {
        return multiValued;
    }

    /**
     * Tells a parameter a run may leave empty from one it always supplies — an optional report parameter, as
     * opposed to a required one. A query referencing an optional parameter has to survive an empty value, which
     * in JPQL means guarding the condition with {@code (:name is null or …)}; generation is told so, and a run
     * binds {@code null} for such a parameter instead of failing.
     *
     * @return {@code true} if a run may bind {@code null} for this parameter
     */
    public boolean isOptional() {
        return optional;
    }

    @Override
    public String toString() {
        return "LlmQueryParameter{name='" + name + "', javaType='" + javaType + "'}";
    }
}

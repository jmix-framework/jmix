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

import org.jspecify.annotations.Nullable;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * A named parameter of an {@link LlmDataQuery}: the name used in the JPQL text, the Java type its value
 * must be bound as, and the value itself when it is known.
 * <p>
 * The same class describes a parameter offered to query generation (where the value is irrelevant) and
 * an argument bound at execution time. The value is never persisted with the generated query, so one
 * generated query serves any set of arguments.
 */
public class LlmQueryParameter {

    protected String name;
    protected String javaType;

    /**
     * Transient on purpose: the value must not become part of the stored query document.
     */
    @Nullable
    protected transient Object value;

    /**
     * Transient for the same reason as the value: it describes what the run offers, not what the stored
     * query declares, and must not become part of the stored document.
     */
    protected transient boolean multiValued;

    public LlmQueryParameter(String name, String javaType, @Nullable Object value) {
        this(name, javaType, value, false);
    }

    public LlmQueryParameter(String name, String javaType, @Nullable Object value, boolean multiValued) {
        checkNotNullArgument(name, "name is null");
        checkNotNullArgument(javaType, "javaType is null");

        this.name = name;
        this.javaType = javaType;
        this.value = value;
        this.multiValued = multiValued;
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
     * @return the value to bind, or {@code null} if this parameter only declares a name and a type
     */
    @Nullable
    public Object getValue() {
        return value;
    }

    /**
     * Tells a parameter that carries several values of {@link #getJavaType()} from an ordinary single-valued
     * one, so that a query can match it with {@code IN}. This may be a collection-valued report parameter or
     * the values of one cross-tab axis. Stated explicitly rather than inferred from the value, because query
     * generation is offered parameters whose value is not known yet.
     *
     * @return {@code true} if the value is a collection of {@link #getJavaType()}
     */
    public boolean isMultiValued() {
        return multiValued;
    }

    @Override
    public String toString() {
        return "LlmQueryParameter{name='" + name + "', javaType='" + javaType + "'}";
    }
}

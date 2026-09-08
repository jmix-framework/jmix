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

package io.jmix.aitools.dataload.generation;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * A parameter that generated query text may reference: its name, the Java type of the value it will
 * be bound to, and how it is bound. Carries no value — a generated query references it by name and is
 * given values only when it runs.
 */
public class EntityDataLoadQueryParameter {

    protected String name;
    protected String javaType;
    protected boolean multiValued;
    protected boolean optional;

    public EntityDataLoadQueryParameter(String name, String javaType) {
        checkNotNullArgument(name, "name is null");
        checkNotNullArgument(javaType, "javaType is null");

        this.name = name;
        this.javaType = javaType;
    }

    /**
     * Marks the parameter as bound to a collection, so a query matches it with {@code IN}.
     *
     * @param multiValued whether the value is a collection of {@link #getJavaType()}
     * @return this parameter
     */
    public EntityDataLoadQueryParameter setMultiValued(boolean multiValued) {
        this.multiValued = multiValued;
        return this;
    }

    /**
     * Marks the parameter as one a caller may leave {@code null}, so a query referencing it must guard the
     * condition.
     *
     * @param optional whether the value may be {@code null}
     * @return this parameter
     */
    public EntityDataLoadQueryParameter setOptional(boolean optional) {
        this.optional = optional;
        return this;
    }

    /**
     * Returns the name a generated query references this parameter by.
     *
     * @return the JPQL parameter name, without the leading colon
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the Java type the value is bound as at execution.
     *
     * @return the fully qualified Java type, or {@code ""} when unknown
     */
    public String getJavaType() {
        return javaType;
    }

    /**
     * Tells a parameter bound as a collection from a single-valued one, so a query matches it with {@code IN}.
     *
     * @return {@code true} if the value is a collection of {@link #getJavaType()}
     */
    public boolean isMultiValued() {
        return multiValued;
    }

    /**
     * Tells a parameter a caller may leave empty from one it always supplies. A query referencing an optional
     * parameter must survive a {@code null} value, which in JPQL means guarding the condition.
     *
     * @return {@code true} if the value may be {@code null}
     */
    public boolean isOptional() {
        return optional;
    }
}

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

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A mutable named query parameter passed to JPQL execution.
 * <p>
 * Holds the parameter name, its declared Java type name (for example {@code "String"} or
 * {@code "java.util.UUID"}) and the raw value. The value is converted to the actual Java type
 * by {@link JpqlParameterConversionService} before the query is run.
 */
@NullMarked
public class JpqlExecutionParameter {

    protected String name;
    protected String type;

    @Nullable
    protected Object value;

    /**
     * @param name  parameter name as used in the JPQL text
     * @param type  declared Java type name of the parameter
     * @param value raw parameter value
     */
    public JpqlExecutionParameter(String name, String type, @Nullable Object value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    /**
     * Returns the parameter name as used in the JPQL text.
     *
     * @return parameter name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the declared Java type name of the parameter.
     *
     * @return Java type name
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the raw, not-yet-converted parameter value.
     *
     * @return parameter value, or {@code null} if none was provided
     */
    @Nullable
    public Object getValue() {
        return value;
    }

    public void setValue(@Nullable Object value) {
        this.value = value;
    }
}

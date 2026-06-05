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
 * A single named query parameter as produced by the JPQL generation layer.
 * <p>
 * The value is still untyped at this stage and is converted to the actual Java type before execution.
 */
@NullMarked
public class GeneratedJpqlParameter {

    protected String name;
    protected String type;

    @Nullable
    protected Object value;

    /**
     * @param name  parameter name as used in the JPQL text
     * @param type  declared Java type name of the parameter (for example {@code "String"} or {@code "java.util.UUID"})
     * @param value raw parameter value
     */
    public GeneratedJpqlParameter(String name, String type, @Nullable Object value) {
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

    /**
     * Returns the declared Java type name of the parameter, for example {@code "String"} or {@code "java.util.UUID"}.
     *
     * @return Java type name
     */
    public String getType() {
        return type;
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

    @Override
    public String toString() {
        return "GeneratedJpqlParameter{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", value=" + value +
                '}';
    }
}

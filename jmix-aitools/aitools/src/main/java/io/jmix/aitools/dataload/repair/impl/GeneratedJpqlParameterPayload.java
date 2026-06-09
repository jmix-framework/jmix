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

package io.jmix.aitools.dataload.repair.impl;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Raw LLM output for a single named query parameter, deserialized from the model's JSON response.
 */
@NullMarked
public class GeneratedJpqlParameterPayload {

    @Nullable
    protected String name;
    @Nullable
    protected String type;
    @Nullable
    protected Object value;

    /**
     * Returns the parameter name.
     *
     * @return parameter name, or {@code null} if the model did not provide one
     */
    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    /**
     * Returns the declared Java type name of the parameter.
     *
     * @return Java type name, or {@code null} if absent
     */
    @Nullable
    public String getType() {
        return type;
    }

    public void setType(@Nullable String type) {
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

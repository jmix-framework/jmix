/*
 * Copyright 2022 Haulmont.
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

package io.jmix.quartzui.screen.trigger;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import javax.annotation.Nullable;

/**
 * Represents predefined scenarios of trigger execution repeating
 */
public enum RepeatMode implements EnumClass<String> {
    /**
     * Execute once without any repeats
     */
    EXECUTE_ONCE("execute_once"),
    /**
     * Performs N additional executions besides the first one.
     */
    FINITE_REPEATS("finite_repeats"),
    /**
     * Performs infinite additional executions
     */
    EXECUTE_FOREVER("execute_forever");

    private final String id;

    RepeatMode(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static RepeatMode fromId(String id) {
        for (RepeatMode at : RepeatMode.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}

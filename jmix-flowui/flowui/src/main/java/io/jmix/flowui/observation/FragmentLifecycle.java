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

package io.jmix.flowui.observation;

import io.jmix.flowui.fragment.Fragment;

/**
 * Enum representing the lifecycle phases of a {@link Fragment}.
 */
public enum FragmentLifecycle {

    /**
     * Phase when the {@link Fragment} instance is created
     */
    CREATE("create"),

    /**
     * Phase when the {@link Fragment} is fully initialized and ready for interaction
     */
    READY("ready");

    private final String name;

    FragmentLifecycle(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Gets the string name representation of the lifecycle phase.
     *
     * @return the name of the lifecycle phase
     */
    public String getName() {
        return name;
    }
}

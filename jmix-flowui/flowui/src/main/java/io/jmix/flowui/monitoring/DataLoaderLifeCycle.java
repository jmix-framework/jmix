/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.monitoring;

import io.jmix.flowui.model.DataLoader;

/**
 * Represents the life cycle stages of a {@link DataLoader}. Can be used to establish monitoring points
 * and to track the specific stages of data loading processes.
 */
public enum DataLoaderLifeCycle {

    /**
     * Represents the stage before data loading begins.
     */
    PRE_LOAD("preLoad"),

    /**
     * Represents the actual data loading process.
     */
    POST_LOAD("postLoad"),

    /**
     * Represents the stage after data loading is completed.
     */
    LOAD("load");

    private final String name;

    DataLoaderLifeCycle(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns the name associated with the current lifecycle stage.
     *
     * @return the name of the lifecycle stage
     */
    public String getName() {
        return name;
    }
}

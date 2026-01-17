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


import io.jmix.flowui.view.View;

/**
 * Enum representing the lifecycle phases of a {@link View}.
 */
public enum ViewLifeCycle {

    /**
     * Phase when the {@link View} instance is created
     */
    CREATE("create"),

    /**
     * Phase when the {@link View} data is being loaded from XML
     */
    LOAD("load"),

    /**
     * Phase when the {@link View} is being initialized
     */
    INIT("init"),

    /**
     * Phase immediately before the {@link View} is shown to the user
     */
    BEFORE_SHOW("beforeShow"),

    /**
     * Phase when the {@link View} is fully initialized and ready for interaction
     */
    READY("ready"),

    /**
     * Phase when dependencies are being injected into the {@link View}
     */
    INJECT("inject"),

    /**
     * Phase immediately before the {@link View} is closed
     */
    BEFORE_CLOSE("beforeClose"),

    /**
     * Phase after the {@link View} has been closed
     */
    AFTER_CLOSE("afterClose");

    private final String name;

    ViewLifeCycle(String name) {
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

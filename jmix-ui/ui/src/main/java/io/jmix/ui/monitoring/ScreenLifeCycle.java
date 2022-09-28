/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.monitoring;

public enum ScreenLifeCycle {
    CREATE("create"),
    LOAD("load"),
    @Deprecated XML("xml"), // not used
    INIT("init"),
    AFTER_INIT("afterInit"),
    BEFORE_SHOW("beforeShow"),
    AFTER_SHOW("afterShow"),
    @Deprecated UI_PERMISSIONS("uiPermissions"), // not used
    INJECTION("inject");

    private String name;

    ScreenLifeCycle(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }
}
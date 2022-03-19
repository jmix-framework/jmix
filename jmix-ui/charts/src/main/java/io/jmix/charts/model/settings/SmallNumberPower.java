/*
 * Copyright 2021 Haulmont.
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

package io.jmix.charts.model.settings;

import io.jmix.charts.model.JsonEnum;

public enum SmallNumberPower implements JsonEnum {
    YOCTO("1e-24"),
    ZEPTO("1e-21"),
    ATTO("1e-18"),
    FEMTO("1e-15"),
    PICO("1e-12"),
    NANO("1e-9"),
    MICRO("1e-6"),
    MILLI("1e-3");

    private String id;

    SmallNumberPower(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }
}
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

package io.jmix.charts.model.date;

import io.jmix.charts.model.JsonEnum;

import javax.annotation.Nullable;

public enum DatePeriod implements JsonEnum {
    MILLISECONDS("fff"),
    SECONDS("ss"),
    MINUTES("mm"),
    HOURS("hh"),
    DAYS("DD"),
    MONTHS("MM"),
    WEEKS("WW"),
    YEARS("YYYY");

    private String id;

    DatePeriod(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static DatePeriod fromId(String id) {
        for (DatePeriod period : values()) {
            if (period.getId().equals(id)) {
                return period;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return id;
    }
}
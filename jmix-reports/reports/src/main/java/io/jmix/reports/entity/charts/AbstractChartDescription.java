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

package io.jmix.reports.entity.charts;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.reports.converter.MetadataFieldsIgnoringGson;

import jakarta.annotation.Nullable;
import jakarta.persistence.Id;
import java.util.UUID;

@JmixEntity(name = "report_AbstractChartDescription")
@SystemLevel
public abstract class AbstractChartDescription {

    @Id
    @JmixGeneratedValue
    protected UUID id;
    @JmixProperty
    protected final String type;
    @JmixProperty
    protected Boolean showLegend;
    @JmixProperty
    protected String customJsonConfig;

    @Nullable
    public static AbstractChartDescription fromJsonString(String jsonString) {
        Preconditions.checkNotNullArgument(jsonString);
        Gson gson = getGson();
        JsonObject jsonElement;

        try {
            jsonElement = gson.fromJson(jsonString, JsonObject.class);
        } catch (JsonSyntaxException e) {
            return null;
        }

        if (jsonElement != null) {
            JsonPrimitive type = jsonElement.getAsJsonPrimitive("type");

            if (type == null) {
                return null;
            }

            if (ChartType.PIE.getId().equals(type.getAsString())) {
                return gson.fromJson(jsonString, PieChartDescription.class);
            } else if (ChartType.SERIAL.getId().equals(type.getAsString())) {
                return gson.fromJson(jsonString, SerialChartDescription.class);
            }
        }

        return null;
    }

    public static String toJsonString(AbstractChartDescription chartDescription) {
        Preconditions.checkNotNullArgument(chartDescription);
        Gson gson = getGson();
        String jsonString = gson.toJson(chartDescription);
        return jsonString;
    }

    protected static Gson getGson() {
        return MetadataFieldsIgnoringGson.create()
                .setIgnoringStrategy()
                .build();
    }

    public AbstractChartDescription(String type) {
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ChartType getType() {
        return ChartType.fromId(type);
    }

    public Boolean getShowLegend() {
        return showLegend;
    }

    public void setShowLegend(Boolean showLegend) {
        this.showLegend = showLegend;
    }

    public void setCustomJsonConfig(String customJsonConfig) {
        this.customJsonConfig = customJsonConfig;
    }

    public String getCustomJsonConfig() {
        return customJsonConfig;
    }
}

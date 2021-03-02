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

package io.jmix.pivottable.component.impl;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.jmix.pivottable.component.PivotTable;

import javax.annotation.Nullable;

/**
 * Helps to retrieve properties from {@link PivotTable} native json.
 */
public final class PivotNativeJsonUtils {

    /**
     * Get editable property from native json.
     *
     * @param json native json configuration of pivot table
     * @return null if there is no such property, otherwise true or false
     */
    @Nullable
    public static Boolean isEditable(String json) {
        if (Strings.isNullOrEmpty(json)) {
            return null;
        }

        JsonObject nativeJson;

        JsonParser parser = new JsonParser();
        try {
            nativeJson = parser.parse(json).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new IllegalStateException("Unable to parse JSON chart configuration");
        }

        if (nativeJson.get("editable") == null) {
            return null;
        } else {
            return nativeJson.get("editable").getAsBoolean();
        }
    }

    /**
     * Get renderer id from native json.
     *
     * @param json native json configuration of pivot table
     * @return null if there is no such property, otherwise renderer id
     */
    @Nullable
    public static String getRenderer(String json) {
        if (Strings.isNullOrEmpty(json)) {
            return null;
        }

        JsonObject nativeJson;

        JsonParser parser = new JsonParser();
        try {
            nativeJson = parser.parse(json).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new IllegalStateException("Unable to parse JSON chart configuration");
        }

        if (nativeJson.get("renderer") == null) {
            return null;
        } else {
            return nativeJson.get("renderer").getAsString();
        }
    }
}

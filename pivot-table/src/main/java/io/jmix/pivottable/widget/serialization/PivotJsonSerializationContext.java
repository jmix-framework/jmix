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

package io.jmix.pivottable.widget.serialization;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import io.jmix.pivottable.model.PivotTableModel;
import org.apache.commons.collections4.MapUtils;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PivotJsonSerializationContext implements JsonSerializationContext {

    protected PivotTableModel pivotTableModel;
    protected Gson gson;

    public PivotJsonSerializationContext(PivotTableModel pivotTableModel, Gson gson) {
        this.pivotTableModel = pivotTableModel;
        this.gson = gson;
    }

    @Override
    public JsonElement serialize(Object src) {
        return gson.toJsonTree(src);
    }

    @Override
    public JsonElement serialize(Object src, Type typeOfSrc) {
        return gson.toJsonTree(src, typeOfSrc);
    }

    public PivotTableModel getPivotTableModel() {
        return pivotTableModel;
    }

    public List<String> getProperties() {
        return Collections.unmodifiableList(pivotTableModel.getWiredFields());
    }

    public String getLocalizedPropertyName(String property) {
        Map<String, String> properties = pivotTableModel.getProperties();
        return (MapUtils.isEmpty(properties) || !properties.containsKey(property))
                ? property
                : properties.get(property);
    }
}
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

package io.jmix.pivottable.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.jmix.pivottable.model.PivotTableModel;
import io.jmix.pivottable.widget.serialization.PivotJsonSerializationContext;
import io.jmix.pivottable.widget.serialization.PivotTableSerializationContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

@Component("ui_PivotTableModelSerializer")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PivotTableModelSerializer {

    public JsonElement serialize(PivotTableModel src, JsonSerializationContext context, Consumer<PivotTableSerializationContext> postSerializationHandler) {
        JsonObject jsonObject = (JsonObject) serialize(src, context);
        if (postSerializationHandler != null) {
            postSerializationHandler.accept(new PivotTableSerializationContext(null, jsonObject, context));
        }

        return jsonObject;
    }

    public JsonElement serialize(PivotTableModel src, JsonSerializationContext context) {
        JsonObject pivotJson = context.serialize(src).getAsJsonObject();

        if (context instanceof PivotJsonSerializationContext) {
            PivotJsonSerializationContext pivotContext = (PivotJsonSerializationContext) context;

            localizeArray(pivotJson, "rows", pivotContext);
            localizeArray(pivotJson, "cols", pivotContext);
            localizeArray(pivotJson, "aggregationProperties", pivotContext);
            localizeArray(pivotJson, "hiddenProperties", pivotContext);
            localizeArray(pivotJson, "hiddenFromAggregations", pivotContext);
            localizeArray(pivotJson, "hiddenFromDragDrop", pivotContext);

            JsonObject aggregation = pivotJson.getAsJsonObject("aggregation");
            if (aggregation != null) {
                localizeArray(aggregation, "properties", pivotContext);
            }

            localizeMapKeys(pivotJson, "inclusions", pivotContext);
            localizeMapKeys(pivotJson, "exclusions", pivotContext);
        }

        return pivotJson;
    }

    protected void localizeArray(JsonObject jsonObject, String property, PivotJsonSerializationContext context) {
        JsonArray array = jsonObject.getAsJsonArray(property);
        if (array != null) {
            JsonArray localizedArray = new JsonArray();
            for (JsonElement row : array) {
                localizedArray.add(context.getLocalizedPropertyName(row.getAsString()));
            }
            jsonObject.add(property, localizedArray);
        }
    }

    private void localizeMapKeys(JsonObject jsonObject, String propery, PivotJsonSerializationContext context) {
        JsonObject map = jsonObject.getAsJsonObject(propery);
        if (map != null) {
            JsonObject localizedMap = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : map.entrySet()) {
                localizedMap.add(context.getLocalizedPropertyName(entry.getKey()), entry.getValue());
            }
            jsonObject.add(propery, localizedMap);
        }
    }
}

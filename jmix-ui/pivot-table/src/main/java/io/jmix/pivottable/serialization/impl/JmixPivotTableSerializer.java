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

package io.jmix.pivottable.serialization.impl;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import io.jmix.pivottable.model.*;
import io.jmix.pivottable.serialization.JsFunctionSerializer;
import io.jmix.pivottable.serialization.JsonEnumSerializer;
import io.jmix.ui.data.DataProvider;
import io.jmix.pivottable.serialization.PivotTableDataItemsSerializer;
import io.jmix.pivottable.serialization.PivotTableModelSerializer;
import io.jmix.pivottable.widget.serialization.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Component("ui_JmixPivotTableSerializer")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JmixPivotTableSerializer implements PivotTableSerializer {

    protected final static Gson gson;

    static {
        // GSON is thread safe so we can use shared GSON instance
        gson = createGsonBuilder().create();
    }

    /**
     * Returns default GSON builder for configuration serializer.
     */
    private static GsonBuilder createGsonBuilder() {
        GsonBuilder builder = new GsonBuilder();
        builder.setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                Expose expose = f.getAnnotation(Expose.class);
                return expose != null && !expose.serialize();
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        });
        setDefaultProperties(builder);
        return builder;
    }

    private static void setDefaultProperties(GsonBuilder builder) {
        // uncomment if you wish to debug generated json
        // builder.setPrettyPrinting();
        builder.registerTypeHierarchyAdapter(JsonEnum.class, new JsonEnumSerializer());
        builder.registerTypeHierarchyAdapter(JsFunction.class, new JsFunctionSerializer());
        builder.registerTypeHierarchyAdapter(DerivedProperties.class, new DerivedPropertiesSerializer());
        builder.registerTypeHierarchyAdapter(UnusedPropertiesVertical.class, new UnusedPropertiesVerticalSerializer());
    }

    protected PivotTableModelSerializer modelSerializer;
    protected PivotTableDataItemsSerializer itemsSerializer;


    @Autowired
    public void setModelSerializer(PivotTableModelSerializer modelSerializer) {
        this.modelSerializer = modelSerializer;
    }

    @Autowired
    public void setItemsSerializer(PivotTableDataItemsSerializer dataItemsSerializer) {
        this.itemsSerializer = dataItemsSerializer;
    }

    @Override
    public String serialize(PivotTableModel pivotTable) {
        return serialize(pivotTable, null);
    }

    @Override
    public String serialize(PivotTableModel pivotTable, Consumer<PivotTableSerializationContext> postSerializationHandler) {
        PivotJsonSerializationContext context = createPivotJsonSerializationContext(pivotTable);
        JsonElement pivotElement = modelSerializer.serialize(pivotTable, context, postSerializationHandler);
        return gson.toJson(pivotElement);
    }

    @Override
    @Nullable
    public String serializeData(PivotTableModel pivotTable) {
        return serializeData(pivotTable, null);
    }

    @Override
    @Nullable
    public String serializeData(PivotTableModel pivotTable, Consumer<PivotTableSerializationContext> postSerializationHandler) {
        DataProvider dataProvider = pivotTable.getDataProvider();
        if (dataProvider != null) {
            PivotJsonSerializationContext context = createPivotJsonSerializationContext(pivotTable);
            JsonElement dataProviderElement =
                    itemsSerializer.serialize(dataProvider.getItems(), context, postSerializationHandler);
            return gson.toJson(dataProviderElement);
        }
        return null;
    }

    protected PivotJsonSerializationContext createPivotJsonSerializationContext(PivotTableModel pivotTable) {
        return new PivotJsonSerializationContext(pivotTable, gson);
    }

    @Override
    public String toJson(Object value) {
        return gson.toJson(value);
    }
}

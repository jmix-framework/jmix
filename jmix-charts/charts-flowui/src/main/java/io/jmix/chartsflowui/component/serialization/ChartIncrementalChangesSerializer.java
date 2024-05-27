/*
 * Copyright 2023 Haulmont.
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

package io.jmix.chartsflowui.component.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.jmix.chartsflowui.kit.component.model.DataSet;
import io.jmix.chartsflowui.kit.component.serialization.ChartIncrementalChanges;
import io.jmix.chartsflowui.kit.data.chart.DataItem;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;

@SuppressWarnings("rawtypes")
@Component("charts_ChartIncrementalChangesSerializer")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ChartIncrementalChangesSerializer extends AbstractDataSerializer<ChartIncrementalChanges> {

    public ChartIncrementalChangesSerializer(Function<DataItem, String> itemKeyMapper) {
        super(ChartIncrementalChanges.class, itemKeyMapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void serializeNonNullValue(ChartIncrementalChanges value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeStartObject();

        writeItems("add", value.getAddedItems(), value.getSource(), gen, provider);
        writeItems("remove", value.getRemovedItems(), value.getSource(), gen, provider);
        writeItems("update", value.getUpdatedItems(), value.getSource(), gen, provider);

        gen.writeEndObject();
    }

    protected void writeItems(String propertyName, @Nullable Collection<? extends DataItem> items,
                              DataSet.Source<?> source, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        if (items == null) {
            return;
        }

        gen.writeArrayFieldStart(propertyName);
        for (DataItem item : items) {
            serializeDataItem(item, gen, provider, source.getCategoryField(), source.getValueFields());
        }
        gen.writeEndArray();
    }
}

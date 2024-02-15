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

package io.jmix.chartsflowui.kit.component.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.jmix.chartsflowui.kit.component.model.Brush;

import java.io.IOException;
import java.util.Arrays;

public class IndexItemSerializer extends AbstractSerializer<Brush.IndexItem> {

    public IndexItemSerializer() {
        super(Brush.IndexItem.class);
    }

    @Override
    public void serializeNonNullValue(Brush.IndexItem value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        if (value.getSingleIndex() != null) {
            gen.writeNumber(value.getSingleIndex());
        } else if (value.getIndexes() != null) {
            Integer[] integerArray = value.getIndexes();
            int[] intArray = Arrays.stream(integerArray).mapToInt(Integer::valueOf).toArray();
            gen.writeArray(intArray, 0, intArray.length);

        } else {
            gen.writeString(value.getBrushSelectMode().getId());
        }
    }
}

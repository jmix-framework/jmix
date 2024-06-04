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
import io.jmix.chartsflowui.kit.component.model.shared.Decal;

import java.io.IOException;
import java.util.Arrays;

public class DashArraySerializer extends AbstractSerializer<Decal.DashArray> {

    public DashArraySerializer() {
        super(Decal.DashArray.class);
    }

    @Override
    public void serializeNonNullValue(Decal.DashArray value, JsonGenerator gen, SerializerProvider provider) throws IOException {

        if (value.getNumber() != null) {
            gen.writeNumber(value.getNumber());

        } else if (value.getArray() != null) {
            int[] intArray = mapToPrimitiveArray(value.getArray());
            gen.writeArray(intArray, 0, intArray.length);

        } else {
            Integer[][] twoDimensionalArray = value.getTwoDimensionalArray();

            gen.writeStartArray();
            for (Integer[] oneDimensionalArray : twoDimensionalArray) {
                int[] intArray = mapToPrimitiveArray(oneDimensionalArray);

                if (intArray.length == 1) {
                    gen.writeNumber(intArray[0]);
                } else {
                    gen.writeArray(intArray, 0, intArray.length);
                }
            }
            gen.writeEndArray();
        }
    }

    protected int[] mapToPrimitiveArray(Integer[] objectArray) {
        return Arrays.stream(objectArray).mapToInt(Integer::valueOf).toArray();
    }
}

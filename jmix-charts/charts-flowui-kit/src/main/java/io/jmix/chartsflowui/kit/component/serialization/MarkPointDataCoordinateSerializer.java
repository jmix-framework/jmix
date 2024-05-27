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
import io.jmix.chartsflowui.kit.component.model.series.mark.Coordinate;

import java.io.IOException;
import java.util.Arrays;

public class MarkPointDataCoordinateSerializer extends AbstractSerializer<Coordinate> {

    public MarkPointDataCoordinateSerializer() {
        super(Coordinate.class);
    }

    @Override
    public void serializeNonNullValue(Coordinate value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        if (value.getStringCoordinates() != null) {
            gen.writeArray(value.getStringCoordinates(), 0, value.getStringCoordinates().length);
        } else if (value.getNumberCoordinates() != null) {
            // WA: gen.writeArray(Double[], int, int) can't be resolved
            double[] doubleArray = Arrays.stream(value.getNumberCoordinates())
                    .mapToDouble(Double::doubleValue)
                    .toArray();

            gen.writeArray(doubleArray, 0, value.getNumberCoordinates().length);
        }
    }
}

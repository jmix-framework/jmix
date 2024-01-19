/*
 * Copyright 2024 Haulmont.
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
import io.jmix.chartsflowui.kit.component.model.series.GaugeSeries;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;

public class GaugeSeriesAxisLineStyleSerializer extends AbstractSerializer<GaugeSeries.AxisLine.LineStyle> {

    public GaugeSeriesAxisLineStyleSerializer() {
        super(GaugeSeries.AxisLine.LineStyle.class);
    }

    @Override
    public void serializeNonNullValue(GaugeSeries.AxisLine.LineStyle value, JsonGenerator gen,
                                      SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        if (value.getColorPalette() != null) {
            Object[] mappedColorPalette = value.getColorPalette().entrySet().stream()
                    .sorted((Comparator.comparingDouble(Map.Entry::getKey)))
                    .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
                    .toArray();

            gen.writeObjectField("color", mappedColorPalette);
        }

        writeNumberField("width", value.getWidth(), gen);
        writeNumberField("shadowBlur", value.getShadowBlur(), gen);

        if (value.getShadowColor() != null) {
            gen.writeStringField("shadowColor", value.getShadowColor().getValue());
        }

        writeNumberField("shadowOffsetX", value.getShadowOffsetX(), gen);
        writeNumberField("shadowOffsetY", value.getShadowOffsetY(), gen);

        if (value.getOpacity() != null) {
            gen.writeNumberField("opacity", value.getOpacity());
        }

        gen.writeEndObject();
    }

    protected void writeNumberField(String fieldName, Integer number, JsonGenerator gen) throws IOException {
        if (number != null) {
            gen.writeNumberField(fieldName, number);
        }
    }
}

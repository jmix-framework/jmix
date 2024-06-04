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
import io.jmix.chartsflowui.kit.component.model.shared.Padding;

import java.io.IOException;

public class PaddingSerializer extends AbstractSerializer<Padding> {

    public PaddingSerializer() {
        super(Padding.class);
    }

    @Override
    public void serializeNonNullValue(Padding value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeArray(new int[]{value.getTop(), value.getRight(), value.getBottom(), value.getLeft()}, 0, 4);
    }
}

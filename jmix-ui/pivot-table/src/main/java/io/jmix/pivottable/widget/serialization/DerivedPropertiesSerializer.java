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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.jmix.pivottable.model.DerivedProperties;
import io.jmix.pivottable.model.JsFunction;

import java.lang.reflect.Type;
import java.util.Map;

public class DerivedPropertiesSerializer implements JsonSerializer<DerivedProperties> {

    private static final String SUFFIX = "Function";

    @Override
    public JsonElement serialize(DerivedProperties src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject derivedAttributes = new JsonObject();
        for (Map.Entry<String, JsFunction> entry : src.getProperties().entrySet()) {
            derivedAttributes.add(entry.getKey() + SUFFIX, context.serialize(entry.getValue()));
        }
        return derivedAttributes;
    }
}

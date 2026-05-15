/*
 * Copyright 2026 Haulmont.
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

package io.jmix.aitools.dataload.generation;

import org.jspecify.annotations.Nullable;

public class GeneratedJpqlParameter {

    protected String name;
    protected String type;
    protected Object value;

    public GeneratedJpqlParameter(String name, String type, @Nullable Object value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Nullable
    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "GeneratedJpqlParameter{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", value=" + value +
                '}';
    }
}

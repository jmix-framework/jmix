/*
 * Copyright 2020 Haulmont.
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

package io.jmix.dynattr;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import io.jmix.core.EntityEntry;

public class ConfigurationExclusionStrategy implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return false;
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return EntityEntry.class.isAssignableFrom(clazz);
    }
}

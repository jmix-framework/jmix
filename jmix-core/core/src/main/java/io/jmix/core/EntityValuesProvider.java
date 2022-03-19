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

package io.jmix.core;

import io.jmix.core.event.AttributeChanges;

import javax.annotation.Nullable;
import java.util.Set;

public interface EntityValuesProvider {

    boolean supportAttribute(String name);

    @Nullable
    <T> T getAttributeValue(String name) throws EntityValueAccessException;

    void setAttributeValue(String name, @Nullable Object value, boolean checkEquals) throws EntityValueAccessException;

    Set<String> getAttributes();

    Set<AttributeChanges.Change> getChanges();
}

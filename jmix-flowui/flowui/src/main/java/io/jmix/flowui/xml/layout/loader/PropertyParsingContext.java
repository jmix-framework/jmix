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

package io.jmix.flowui.xml.layout.loader;

import io.jmix.flowui.xml.layout.ComponentLoader;
import org.springframework.lang.Nullable;

/**
 * An object that stores information about the value to be set for an object property.
 *
 * @param target       an object to set value
 * @param propertyName property name
 * @param value        string value representation
 * @param type         optional type that helps determine actual parser
 */
public record PropertyParsingContext(Object target,
                                     String propertyName,
                                     String value,
                                     @Nullable String type,
                                     ComponentLoader.Context context) {
}

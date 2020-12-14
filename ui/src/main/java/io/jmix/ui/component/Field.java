/*
 * Copyright 2019 Haulmont.
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
package io.jmix.ui.component;

import io.jmix.ui.component.data.HasValueSource;

/**
 * Base interface for "fields" - components intended to display and edit value of a certain entity attribute.
 */
public interface Field<V> extends HasValueSource<V>, Component.HasCaption,
        HasValue<V>, Component.Editable, Component.BelongToFrame, Validatable, Component.HasIcon,
        HasContextHelp, HasHtmlCaption, HasHtmlDescription, HasHtmlSanitizer, HasValidator<V>,
        Requirable {
}

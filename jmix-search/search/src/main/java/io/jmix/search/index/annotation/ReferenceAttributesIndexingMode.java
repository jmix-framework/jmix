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

package io.jmix.search.index.annotation;

/**
 * <p>Enum representing the indexing mode for reference attributes in an entity or data structure.</p>
 *
 * <p>This enum is used to configure how reference attributes are handled during the indexing process.</p>
 * <p>It provides two modes:</p>
 * <ul>
 * <li>{@code NONE}: Disables indexing of reference attributes. No reference attribute values will be indexed.</li>
 * <li>{@code INSTANCE_NAME_ONLY}: Indexes only the instance name of reference attributes. Useful for indexing
 *    a textual representative of a reference without considering the full details of the reference.</li>
 * </ul>
 * <p>This enum is typically used in annotations or configurations related to indexing.</p>
 */
public enum ReferenceAttributesIndexingMode {
    NONE, INSTANCE_NAME_ONLY
}

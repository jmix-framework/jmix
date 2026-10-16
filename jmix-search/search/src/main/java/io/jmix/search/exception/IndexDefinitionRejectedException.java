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

package io.jmix.search.exception;

/**
 * A single index definition cannot be applied, and only that definition is dropped.
 * <p>
 * The entity of the definition is left out of indexing and searching, and every other entity of the application
 * keeps working. This is what the module did before these checks existed: a definition that could not produce a
 * usable index cost its own entity and nothing more. A problem that is not confined to one definition - a
 * malformed application-wide property, for instance - is not reported with this type.
 */
public class IndexDefinitionRejectedException extends IndexConfigurationException {

    public IndexDefinitionRejectedException(String message) {
        super(message);
    }
}

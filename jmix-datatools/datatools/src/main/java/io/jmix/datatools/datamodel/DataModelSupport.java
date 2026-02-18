/*
 * Copyright 2025 Haulmont.
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

package io.jmix.datatools.datamodel;

import io.jmix.datatools.datamodel.entity.EntityModel;

import java.util.List;

/**
 * The central management point for data model manipulations
 */
public interface DataModelSupport {

    /**
     * Provides access to {@link DataModelProvider}
     *
     * @return {@link DataModelProvider} reference
     */
    DataModelProvider getDataModelProvider();

    /**
     * Generate a PNG representation of a diagram as a byte array.
     *
     * @return PNG representation of a diagram as a byte array
     */
    byte[] generateDiagram(List<EntityModel> models);
}
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

package io.jmix.datatools.datamodelvisualization;

import net.sourceforge.plantuml.core.DiagramDescription;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 */
public interface DataModelVisualization {

    /**
     * Writes diagram description to output stream and converts it to complete diagram description
     * that contains a {@link String} diagram representation
     * @param outputStream for writing diagram description data
     * @return diagram description, that contains a {@link String} diagram representation.
     * Diagram description with error code otherwise
     */
    DiagramDescription createStringReader(ByteArrayOutputStream outputStream) throws IOException;
}

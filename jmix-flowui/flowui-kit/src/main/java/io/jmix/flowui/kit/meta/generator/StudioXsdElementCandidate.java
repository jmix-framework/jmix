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

package io.jmix.flowui.kit.meta.generator;

import org.w3c.dom.Element;

import java.nio.file.Path;
import java.util.List;

record StudioXsdElementCandidate(Path schemaPath, String elementName, boolean nested,
                                 List<String> ancestorElements, List<String> contextNames, String description,
                                 StudioMetaDescriptionGenerator.SchemaDocument document, Element element) {

    StudioXsdElementCandidate(Path schemaPath,
                              String elementName,
                              boolean nested,
                              List<String> ancestorElements,
                              List<String> contextNames,
                              String description,
                              StudioMetaDescriptionGenerator.SchemaDocument document,
                              Element element) {
        this.schemaPath = schemaPath;
        this.elementName = elementName;
        this.nested = nested;
        this.ancestorElements = List.copyOf(ancestorElements);
        this.contextNames = List.copyOf(contextNames);
        this.description = description;
        this.document = document;
        this.element = element;
    }

    @Override
    public String toString() {
        return description;
    }
}

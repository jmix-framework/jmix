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

package io.jmix.datatoolsflowui.datamodel;

import io.jmix.flowui.view.View;

/**
 * Provides support for opening a view that visualizes a data model diagram.
 */
public interface DataModelDiagramViewSupport {

    /**
     * Open a view that displays data model diagram.
     *
     * @param origin      the view from which the diagram is opened
     * @param diagramData the diagram data to display
     */
    void open(View<?> origin, byte[] diagramData);
}

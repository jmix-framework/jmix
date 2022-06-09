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

package io.jmix.ui.component;

import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperty;

/**
 * Component that makes a data binding to load data by pages. It contains label with current items count
 * and navigation buttons (next, last etc).
 */
@StudioComponent(
        caption = "SimplePagination",
        category = "Components",
        xmlElement = "simplePagination",
        icon = "io/jmix/ui/icon/component/simplePagination.svg",
        canvasBehaviour = CanvasBehaviour.LABEL,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/simple-pagination.html",
        unsupportedProperties = {"responsive", "enable"}
)
public interface SimplePagination extends PaginationComponent {

    String NAME = "simplePagination";

    /**
     * @return whether items count should be loaded automatically
     */
    boolean isAutoLoad();

    /**
     * Sets whether items count should be loaded automatically. When the autoload is disabled the component
     * doesn't know the total count of items and shows a button with {@code [?]}. When it's enabled the component
     * makes a query to get the total count of items and shows it. The default value is {@code false}.
     *
     * @param autoLoad pass true to enable auto load, or false otherwise
     */
    @StudioProperty(defaultValue = "false")
    void setAutoLoad(boolean autoLoad);
}

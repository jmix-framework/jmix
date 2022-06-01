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

import io.jmix.ui.meta.MaxOccurNumber;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElementsGroup;
import io.jmix.ui.meta.StudioFacet;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.InstanceContainer;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Controls triggering of data loaders and provides support for automatic linking data loaders to data containers
 * and visual components.
 */
@StudioFacet(
        xmlElement = "dataLoadCoordinator",
        caption = "DataLoadCoordinator",
        category = "Facets",
        description = "Controls triggering of data loaders and provides support for automatic linking data loaders to " +
                "data containers and visual components",
        defaultProperty = "id",
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/facets/data-load-coordinator.html",
        icon = "io/jmix/ui/icon/facet/dataLoadCoordinator.svg"
)
public interface DataLoadCoordinator extends Facet {

    String DEFAULT_CONTAINER_PREFIX = "container_";
    String DEFAULT_COMPONENT_PREFIX = "component_";

    /**
     * Sets parameter prefix to denote a data container.
     */
    @StudioProperty(name = "containerPrefix", defaultValue = DEFAULT_CONTAINER_PREFIX)
    void setContainerPrefix(String value);

    /**
     * Sets parameter prefix to denote a visual component.
     */
    @StudioProperty(name = "componentPrefix", defaultValue = DEFAULT_COMPONENT_PREFIX)
    void setComponentPrefix(String value);

    /**
     * Adds trigger on screen/fragment event.
     *
     * @param loader     loader
     * @param eventClass event class
     */
    void addOnFrameOwnerEventLoadTrigger(DataLoader loader, Class eventClass);

    /**
     * Adds trigger on data container {@code ItemChangeEvent}.
     *
     * @param loader    loader
     * @param container master data container
     * @param param     loader parameter
     */
    void addOnContainerItemChangedLoadTrigger(DataLoader loader, InstanceContainer container, @Nullable String param);

    /**
     * Adds trigger on visual component {@code ValueChangeEvent}.
     *
     * @param loader     loader
     * @param component  component which must implement {@code HasValue}
     * @param param      loader parameter
     * @param likeClause whether the condition using the parameter is a LIKE clause
     */
    void addOnComponentValueChangedLoadTrigger(DataLoader loader, Component component, @Nullable String param, LikeClause likeClause);

    /**
     * Configures triggers automatically relying upon parameter prefixes. All data containers that don't have a prefixed
     * parameter in the query string, are configured to be triggered on {@code BeforeShowEvent} or {@code AttachEvent}.
     */
    @StudioProperty(name = "auto", type = PropertyType.BOOLEAN, defaultValue = "false", initialValue = "true")
    void configureAutomatically();

    /**
     * Returns configured triggers.
     */
    @StudioElementsGroup(caption = "Refresh",
            xmlElement = "refresh",
            icon = "io/jmix/ui/icon/facet/refresh.svg",
            maxOccurs = MaxOccurNumber.UNBOUNDED
    )
    @StudioProperties(
            properties = {
                    @StudioProperty(name = "loader", type = PropertyType.DATALOADER_REF, required = true)
            }
    )
    List<Trigger> getTriggers();

    /**
     * Load trigger.
     */
    interface Trigger {
        DataLoader getLoader();
    }

    /**
     * Type of the LIKE clause.
     */
    enum LikeClause {
        /**
         * Not a LIKE clause.
         */
        NONE,

        /**
         * Case-sensitive LIKE. A parameter value will be wrapped in {@code %value%}.
         */
        CASE_SENSITIVE,

        /**
         * Case-insensitive LIKE. A parameter value will be wrapped in {@code (?i)%value%}.
         */
        CASE_INSENSITIVE
    }
}

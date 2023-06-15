/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.facet;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.InstanceContainer;

import org.springframework.lang.Nullable;
import java.util.List;

public interface DataLoadCoordinator extends Facet {

    String NAME = "dataLoadCoordinator";

    String DEFAULT_CONTAINER_PREFIX = "container_";
    String DEFAULT_COMPONENT_PREFIX = "component_";

    /**
     * Sets parameter prefix to denote a data container.
     */
    void setContainerPrefix(String value);

    /**
     * Sets parameter prefix to denote a visual component.
     */
    void setComponentPrefix(String value);

    /**
     * Adds trigger on view event.
     *
     * @param loader     loader
     * @param eventClass event class
     */
    void addOnViewEventLoadTrigger(DataLoader loader, Class<?> eventClass);

    /**
     * Adds trigger on data container {@code ItemChangeEvent}.
     *
     * @param loader    loader
     * @param container master data container
     * @param param     loader parameter
     */
    void addOnContainerItemChangedLoadTrigger(DataLoader loader, InstanceContainer<?> container,
                                              @Nullable String param);

    /**
     * Adds trigger on visual component {@code ValueChangeEvent}.
     *
     * @param loader     loader
     * @param component  component which must implement {@code HasValue}
     * @param param      loader parameter
     * @param likeClause whether the condition using the parameter is a LIKE clause
     */
    void addOnComponentValueChangedLoadTrigger(DataLoader loader, Component component,
                                               @Nullable String param, LikeClause likeClause);

    /**
     * Configures triggers automatically relying upon parameter prefixes. All data containers that don't have a prefixed
     * parameter in the query string, are configured to be triggered on {@code BeforeShowEvent} or {@code AttachEvent}.
     */
    void configureAutomatically();

    /**
     * Returns configured triggers.
     */
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

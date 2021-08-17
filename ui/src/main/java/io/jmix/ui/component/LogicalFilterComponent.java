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

import io.jmix.core.metamodel.datatype.impl.EnumClass;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.model.DataLoader;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * Component which can contain other filter components and can be used for filtering entities
 * returned by the {@link DataLoader}. The component is related to {@link LogicalCondition} which
 * will be used together with query when loading entities into the {@link DataLoader}.
 */
public interface LogicalFilterComponent extends FilterComponent {

    /**
     * @return a {@link LogicalCondition} related to the current component
     */
    @Override
    LogicalCondition getQueryCondition();

    /**
     * Adds a {@link FilterComponent} to the component. Updates the current {@link LogicalCondition}
     * by adding a {@link Condition} from the {@link FilterComponent}.
     *
     * @param filterComponent a {@link FilterComponent} to add
     * @see FilterComponent#getQueryCondition()
     */
    @StudioElement
    void add(FilterComponent filterComponent);

    /**
     * Removes a {@link FilterComponent} from the component. Updates the current {@link LogicalCondition}.
     *
     * @param filterComponent a {@link FilterComponent} to remove
     */
    void remove(FilterComponent filterComponent);

    /**
     * Removes all filter components from the component. Resets the current {@link LogicalCondition}.
     */
    void removeAll();

    /**
     * @return the list of filter components directly owned by the current component
     */
    List<FilterComponent> getOwnFilterComponents();

    /**
     * @return the list of filter components belonging to the whole components tree below
     * this component
     */
    List<FilterComponent> getFilterComponents();

    /**
     * @return a filtering operation
     */
    Operation getOperation();

    /**
     * Sets a filtering operation.
     *
     * @param operation a filtering operation
     */
    @StudioProperty(type = PropertyType.ENUMERATION, initialValue = "AND", options = {"AND", "OR"}, required = true)
    void setOperation(Operation operation);

    /**
     * @return whether to show operation caption
     */
    boolean isOperationCaptionVisible();

    /**
     * Sets whether to show operation caption.
     *
     * @param operationCaptionVisible whether to show operation caption
     */
    @StudioProperty(defaultValue = "true")
    void setOperationCaptionVisible(boolean operationCaptionVisible);

    /**
     * Operation representing corresponding logical filtering condition.
     */
    enum Operation implements EnumClass<String> {
        AND,
        OR;

        @Nullable
        public static Operation fromId(String id) {
            for (Operation operation : Operation.values()) {
                if (Objects.equals(id, operation.getId())) {
                    return operation;
                }
            }
            return null;
        }

        @Override
        public String getId() {
            return name();
        }
    }
}

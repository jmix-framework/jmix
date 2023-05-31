/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.component.logicalfilter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.metamodel.datatype.impl.EnumClass;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.flowui.component.filer.FilterComponent;
import io.jmix.flowui.model.DataLoader;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Component which can contain other filter components and can be used for filtering entities
 * returned by the {@link DataLoader}. The component is related to {@link LogicalCondition} which
 * will be used together with query when loading entities into the {@link DataLoader}.
 */
public interface LogicalFilterComponent<C extends Component & LogicalFilterComponent<C>> extends FilterComponent {

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
    void setOperation(Operation operation);

    /**
     * @return whether to show operation caption
     */
    boolean isOperationTextVisible();

    /**
     * Sets whether to show operation caption.
     *
     * @param operationTextVisible whether to show operation caption
     */
    void setOperationTextVisible(boolean operationTextVisible);

    Registration addFilterComponentsChangeListener(ComponentEventListener<FilterComponentsChangeEvent<C>> listener);

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

    class FilterComponentsChangeEvent<C extends Component & LogicalFilterComponent<C>> extends ComponentEvent<C> {
        public FilterComponentsChangeEvent(C source, boolean fromClient) {
            super(source, fromClient);
        }
    }
}

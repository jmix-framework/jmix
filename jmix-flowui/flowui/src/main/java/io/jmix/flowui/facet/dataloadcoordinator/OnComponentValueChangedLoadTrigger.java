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

package io.jmix.flowui.facet.dataloadcoordinator;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import io.jmix.core.DevelopmentException;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.facet.DataLoadCoordinator;
import io.jmix.flowui.facet.DataLoadCoordinator.LikeClause;
import io.jmix.flowui.model.DataLoader;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Implementation of the {@link DataLoadCoordinator.Trigger} interface that triggers data loading
 * in a {@link DataLoader} when the value of a specified UI component changes. The component must
 * implement the {@code HasValue} interface.
 * <p>
 * This trigger listens for {@code ValueChangeEvent} on the specified component and updates the data
 * loader's parameter with the new value before initiating a data load. It can also handle LIKE clause
 * formatting for string parameters based on the {@link DataLoadCoordinator.LikeClause} configuration.
 */
public class OnComponentValueChangedLoadTrigger extends AbstractParameterLoadTrigger {

    private static final Logger log = LoggerFactory.getLogger(OnComponentValueChangedLoadTrigger.class);

    protected final HasValue<?, ?> component;
    protected final LikeClause likeClause;

    /**
     * @deprecated use {@link #OnComponentValueChangedLoadTrigger(DataLoader, Component, String, LikeClause,
     * DataLoadCoordinator)}
     */
    @Deprecated(since = "3.1.1", forRemoval = true)
    public OnComponentValueChangedLoadTrigger(DataLoader loader, Component component,
                                              String param, LikeClause likeClause) {
        this(loader, component, param, likeClause, null);
    }

    /**
     * Creates a trigger that also sets the missing parameters of the coordinator's other parameter triggers
     * of the same loader before loading.
     *
     * @param loader      loader
     * @param component   component which must implement {@code HasValue}
     * @param param       loader parameter
     * @param likeClause  whether the condition using the parameter is a LIKE clause
     * @param coordinator the coordinator that owns the trigger
     * @see AbstractParameterLoadTrigger
     */
    public OnComponentValueChangedLoadTrigger(DataLoader loader, Component component,
                                              String param, LikeClause likeClause,
                                              @Nullable DataLoadCoordinator coordinator) {
        super(loader, param, coordinator);
        checkNotNullArgument(component);
        checkNotNullArgument(likeClause);

        if (!(component instanceof HasValue)) {
            throw new DevelopmentException(String.format(
                    "Invalid component type in load trigger: %s. Expected " + HasValue.class.getSimpleName(),
                    component.getClass().getName()));
        }

        this.likeClause = likeClause;
        this.component = (HasValue<?, ?>) component;

        this.component.addValueChangeListener(event -> load());
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    @Override
    protected Object getParameterValue() {
        Object value = component instanceof SupportsTypedValue
                ? ((SupportsTypedValue) component).getTypedValue()
                : component.getValue();
        if (value != null && likeClause != LikeClause.NONE) {
            if (!(value instanceof String)) {
                log.warn("Like clause with non-string parameter. The value is passed as is without wrapping in %.");
            } else {
                value = "%" + value + "%";
                if (likeClause == LikeClause.CASE_INSENSITIVE) {
                    value = "(?i)" + value;
                }
            }
        }
        return value;
    }
}

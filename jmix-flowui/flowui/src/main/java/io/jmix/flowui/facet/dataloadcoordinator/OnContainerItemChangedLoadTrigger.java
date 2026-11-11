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

import io.jmix.flowui.facet.DataLoadCoordinator;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.InstanceContainer.ItemChangeEvent;
import org.jspecify.annotations.Nullable;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * This class implements a load trigger for a {@link DataLoadCoordinator}.
 * It is responsible for triggering data loading into an associated {@link DataLoader}
 * whenever the item in a bound {@link InstanceContainer} changes.
 * <p>
 * The class listens to {@link ItemChangeEvent} events from the attached container
 * and ensures that the corresponding {@link DataLoader} reloads data by updating
 * the specified parameter with the current item from the container.
 */
public class OnContainerItemChangedLoadTrigger extends AbstractParameterLoadTrigger {

    protected final InstanceContainer<?> container;

    /**
     * @deprecated use {@link #OnContainerItemChangedLoadTrigger(DataLoader, InstanceContainer, String,
     * DataLoadCoordinator)}
     */
    @Deprecated(since = "3.1.1", forRemoval = true)
    public OnContainerItemChangedLoadTrigger(DataLoader loader, InstanceContainer<?> container, String param) {
        this(loader, container, param, null);
    }

    /**
     * Creates a trigger that also sets the missing parameters of the coordinator's other parameter triggers
     * of the same loader before loading.
     *
     * @param loader      loader
     * @param container   master data container
     * @param param       loader parameter
     * @param coordinator the coordinator that owns the trigger
     * @see AbstractParameterLoadTrigger
     */
    public OnContainerItemChangedLoadTrigger(DataLoader loader, InstanceContainer<?> container, String param,
                                             @Nullable DataLoadCoordinator coordinator) {
        super(loader, param, coordinator);
        checkNotNullArgument(container);

        this.container = container;

        container.addItemChangeListener(event -> load());
    }

    @Nullable
    @Override
    protected Object getParameterValue() {
        return container.getItemOrNull();
    }
}

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

package io.jmix.flowui.data.value;

import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.ValueSourceProvider;
import io.jmix.flowui.model.InstanceContainer;

/**
 * A provider of {@link ValueSource} instances based on a given {@link InstanceContainer}.
 *
 * @param <E> the type of entity that the associated {@link InstanceContainer} works with
 */
public class ContainerValueSourceProvider<E> implements ValueSourceProvider {

    protected final InstanceContainer<E> container;

    public ContainerValueSourceProvider(InstanceContainer<E> container) {
        this.container = container;
    }

    /**
     * Returns the {@link InstanceContainer} associated with this provider.
     *
     * @return the associated {@link InstanceContainer}
     */
    public InstanceContainer<E> getContainer() {
        return container;
    }

    @Override
    public ValueSource<?> getValueSource(String property) {
        return new ContainerValueSource<>(container, property);
    }
}


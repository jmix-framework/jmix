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

public class OnContainerItemChangedLoadTrigger implements DataLoadCoordinator.Trigger {

    protected final DataLoader loader;
    protected final InstanceContainer<?> container;
    protected final String param;

    public OnContainerItemChangedLoadTrigger(DataLoader loader, InstanceContainer<?> container, String param) {
        this.loader = loader;
        this.container = container;
        this.param = param;

        container.addItemChangeListener(event -> load());
    }

    protected void load() {
        loader.setParameter(param, container.getItemOrNull());
        loader.load();
    }

    @Override
    public DataLoader getLoader() {
        return loader;
    }
}

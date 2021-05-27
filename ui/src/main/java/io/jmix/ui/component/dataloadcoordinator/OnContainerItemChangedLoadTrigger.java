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

package io.jmix.ui.component.dataloadcoordinator;

import io.jmix.ui.component.DataLoadCoordinator;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.InstanceContainer;

@StudioElement(
        caption = "OnContainerItemChanged Trigger",
        xmlElement = "onContainerItemChanged",
        icon = "io/jmix/ui/icon/facet/onContainerItemChangedLoadTrigger.svg"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "container", type = PropertyType.DATACONTAINER_REF, required = true),
                @StudioProperty(name = "param", type = PropertyType.STRING)
        }
)
public class OnContainerItemChangedLoadTrigger implements DataLoadCoordinator.Trigger {

    private final DataLoader loader;
    private final InstanceContainer container;
    private final String param;

    public OnContainerItemChangedLoadTrigger(DataLoader loader, InstanceContainer container, String param) {
        this.loader = loader;
        this.container = container;
        this.param = param;
        //noinspection unchecked
        container.addItemChangeListener(event -> load());
    }

    private void load() {
        loader.setParameter(param, container.getItemOrNull());
        loader.load();
    }

    @Override
    public DataLoader getLoader() {
        return loader;
    }
}

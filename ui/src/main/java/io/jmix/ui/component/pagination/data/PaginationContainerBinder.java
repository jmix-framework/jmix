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

package io.jmix.ui.component.pagination.data;

import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.model.BaseCollectionLoader;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.HasLoader;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@StudioElement(
        caption = "Container Provider",
        xmlElement = "containerProvider",
        icon = "io/jmix/ui/icon/element/provider.svg"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "dataContainer", type = PropertyType.COLLECTION_DATACONTAINER_REF, required = true)
        }
)
@Component("ui_PaginationContainerProvider")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PaginationContainerBinder extends AbstractPaginationDataBinder {

    public PaginationContainerBinder(CollectionContainer container) {
        this.container = container;

        if (container instanceof HasLoader) {
            DataLoader loader = ((HasLoader) container).getLoader();
            if (loader instanceof BaseCollectionLoader) {
                this.loader = (BaseCollectionLoader) loader;
            }
        }

        attachCollectionChangeListener();
    }
}

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
import io.jmix.ui.model.*;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@StudioElement(
        caption = "Loader Provider",
        xmlElement = "loaderProvider",
        icon = "io/jmix/ui/icon/element/provider.svg"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "loaderId", type = PropertyType.DATALOADER_REF, required = true)
        }
)
@Component("ui_PaginationLoaderProvider")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PaginationLoaderBinder extends AbstractPaginationDataBinder {

    public PaginationLoaderBinder(BaseCollectionLoader loader) {
        this.loader = loader;
        this.container = loader.getContainer();

        attachCollectionChangeListener();
    }
}

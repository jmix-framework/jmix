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

package io.jmix.flowui.xml.layout.loader.component;

import io.jmix.flowui.component.grid.JmixTreeGridDataProvider;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.exception.GuiDevelopmentException;

public class TreeDataGridLoader extends AbstractGridLoader<TreeDataGrid<?>> {

    @Override
    protected TreeDataGrid<?> createComponent() {
        return factory.create(TreeDataGrid.class);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void setupDataProvider(GridDataHolder holder) {
        Boolean showOrphans = loadBoolean(element, "showOrphans")
                .orElse(false);

        String hierarchyProperty = loadString(element, "hierarchyProperty")
                .orElseThrow(
                        () -> new GuiDevelopmentException(
                                String.format(
                                        "%s doesn't have 'hierarchyProperty' attribute",
                                        resultComponent.getClass().getSimpleName()
                                ),
                                context,
                                "Component ID",
                                resultComponent.getId()
                                        .orElse("null")
                        )
                );

        resultComponent.setHierarchyColumn(hierarchyProperty);

        if (holder.getContainer() != null) {
            resultComponent.setDataProvider(
                    new JmixTreeGridDataProvider(holder.getContainer(), hierarchyProperty, showOrphans)
            );
        }
    }
}

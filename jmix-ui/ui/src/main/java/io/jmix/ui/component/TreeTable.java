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
package io.jmix.ui.component;

import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.PropertiesConstraint;
import io.jmix.ui.meta.PropertiesGroup;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;
import org.springframework.core.ParameterizedTypeReference;

/**
 * TreeTable extends the {@link Table} component so that it can also visualize a hierarchy of its Items in a similar
 * manner that {@link Tree} does. The tree hierarchy is always displayed in the first actual column of the TreeTable.
 *
 * @param <E> row item type
 */
@StudioComponent(
        caption = "TreeTable",
        category = "Components",
        xmlElement = "treeTable",
        icon = "io/jmix/ui/icon/component/treeTable.svg",
        canvasBehaviour = CanvasBehaviour.TABLE,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/tree-table.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "hierarchyProperty", type = PropertyType.PROPERTY_PATH_REF, typeParameter = "E",
                        required = true, options = {"to_one", "to_many"}),
                @StudioProperty(name = "showOrphans", type = PropertyType.BOOLEAN, defaultValue = "true")
        },
        groups = {
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "hierarchyProperty"})
        }
)
public interface TreeTable<E> extends Table<E> {

    String NAME = "treeTable";

    static <T> ParameterizedTypeReference<TreeTable<T>> of(Class<T> itemClass) {
        return new ParameterizedTypeReference<TreeTable<T>>() {};
    }

    void expandAll();

    void expand(Object itemId);

    void collapseAll();

    void collapse(Object itemId);

    /**
     * Expand tree table including specified level
     *
     * @param level level of TreeTable nodes to expand, if passed level = 1 then root items will be expanded
     * @throws IllegalArgumentException if level &lt; 1
     */
    void expandUpTo(int level);

    int getLevel(Object itemId);

    boolean isExpanded(Object itemId);
}

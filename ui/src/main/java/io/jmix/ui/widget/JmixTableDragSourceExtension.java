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

package io.jmix.ui.widget;

import io.jmix.ui.widget.client.table.JmixTableDragSourceExtensionServerRpc;
import com.vaadin.ui.dnd.DragSourceExtension;
import com.vaadin.v7.ui.Table;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Drag source extension for {@link com.vaadin.v7.ui.Table}.
 * <p>
 * You can drag rows from table and drop them to another components if they use special extension to handle drop event.
 * <p>
 * To get dragged item ids in the drop event you get this extension and use {@link #getLastDraggedItemIds()} or
 * {@link #getLastSingleDraggedItemId()}.
 *
 * @param <T> component that extends {@link com.vaadin.v7.ui.Table}
 */
public class JmixTableDragSourceExtension<T extends Table & JmixEnhancedTable> extends DragSourceExtension<T> {

    protected List<Object> transferredItems = new ArrayList<>();

    public JmixTableDragSourceExtension(T target) {
        super(target);

        JmixTableDragSourceExtensionServerRpc serverRpc = (JmixTableDragSourceExtensionServerRpc) rowKeys -> {
            transferredItems.clear();

            for (String key : rowKeys)
                transferredItems.add(target.getItemByRowKey(key));
        };

        registerRpc(serverRpc);
    }

    /**
     * @return list of last dragged item ids
     */
    public List<Object> getLastDraggedItemIds() {
        return transferredItems;
    }

    /**
     * @return id of dragged item. If were dragged more than one it returns first item id in the collection.
     */
    @Nullable
    public Object getLastSingleDraggedItemId() {
        return transferredItems.isEmpty() ? null : transferredItems.iterator().next();
    }
}

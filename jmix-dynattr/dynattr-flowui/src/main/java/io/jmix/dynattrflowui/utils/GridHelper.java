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

package io.jmix.dynattrflowui.utils;

import com.vaadin.flow.component.grid.Grid;
import io.jmix.flowui.model.CollectionContainer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("dynat_GridHelper")
public class GridHelper {
    public <T> void moveTableItemUp(CollectionContainer<T> collectionContainer, Grid<T> table, Runnable afterMoveCommand) {
        List<T> items = collectionContainer.getMutableItems();
        T currentItem = collectionContainer.getItemOrNull();
        if(currentItem == null) {
            currentItem = table.getSelectedItems().iterator().next();
        }
        int idx = items.indexOf(currentItem);
        if (idx == 0) return;
        items.remove(currentItem);
        items.add(idx - 1, currentItem);
        table.select(currentItem);
        collectionContainer.setItem(currentItem);
        if (afterMoveCommand != null) {
            afterMoveCommand.run();
        }
        table.getDataProvider().refreshAll();
    }

    public <T> void moveTableItemDown(CollectionContainer<T> collectionContainer, Grid<T> table, Runnable afterMoveCommand) {
        List<T> items = collectionContainer.getMutableItems();
        T currentItem = collectionContainer.getItemOrNull();
        if(currentItem == null) {
            currentItem = table.getSelectedItems().iterator().next();
        }
        int idx = items.indexOf(currentItem);
        if (idx == items.size() - 1) return;
        items.remove(currentItem);
        items.add(idx + 1, currentItem);
        table.select(currentItem);
        collectionContainer.setItem(currentItem);
        if (afterMoveCommand != null) {
            afterMoveCommand.run();
        }
        table.getDataProvider().refreshAll();
    }
}

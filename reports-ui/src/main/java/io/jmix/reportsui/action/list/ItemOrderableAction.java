/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reportsui.action.list;

import io.jmix.reports.entity.wizard.OrderableEntity;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.list.SecuredListAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.data.DataUnit;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.model.CollectionContainer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Move items in ListComponent up or down. Items in datasource must to be of type {@link OrderableEntity}. <br>
 * Note that order num of item will be changed for moving.
 * Move algorithm is differ from selected items count:
 * <ul>
 *     <li>swap items algorithm if one item is selected </li>
 *     <li>index recalculating algorithm if more than one item selected)</li>
 * </ul>
 */
@ActionType(ItemOrderableAction.ID)
public class ItemOrderableAction<E extends OrderableEntity> extends SecuredListAction {

    public static final String ID = "itemOrderable";

    protected Direction direction;

    public ItemOrderableAction() {
        this(ID);
    }

    public ItemOrderableAction(String actionId) {
        super(actionId);
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    public void actionPerform(Component component) {
        if (!hasSubscriptions(ActionPerformedEvent.class)) {
            execute();
        } else {
            super.actionPerform(component);
        }
    }

    protected void execute() {
        if (target == null) {
            throw new IllegalStateException("OrderableAction target is not set");
        }

        int selectedCnt = target.getSelected().size();
        if (selectedCnt == 1) {
            swapSingleItemWithNeighbour();
        } else if (selectedCnt > 1) {
            moveFewItems();
        }
    }

    /**
     * Swap items is simple
     */
    protected void swapSingleItemWithNeighbour() {
        OrderableEntity selectedItem = (OrderableEntity) target.getSingleSelected();
        OrderableEntity neighbourItem = null;
        List<OrderableEntity> allItems = getItems();
        for (ListIterator<OrderableEntity> iterator = allItems.listIterator(); iterator.hasNext(); ) {
            if (iterator.next().equals(target.getSingleSelected())) {
                neighbourItem = getItemNeighbour(iterator);
                break;
            }
        }
        if (neighbourItem != null) {
            switch (direction) {
                case UP:
                    neighbourItem.setOrderNum(neighbourItem.getOrderNum() + 1);
                    selectedItem.setOrderNum(selectedItem.getOrderNum() - 1);
                    break;
                case DOWN:
                    neighbourItem.setOrderNum(neighbourItem.getOrderNum() - 1);
                    selectedItem.setOrderNum(selectedItem.getOrderNum() + 1);
                    break;
            }
            sortTableDsByItemsOrderNum();
        }
    }

    @Nullable
    protected OrderableEntity getItemNeighbour(ListIterator<OrderableEntity> iterator) {
        OrderableEntity neighbourItem = null;
        switch (direction) {
            case UP:
                iterator.previous(); //lets go 1 step back
                if (iterator.hasPrevious()) {
                    neighbourItem = iterator.previous();
                }
                break;
            case DOWN:
                if (iterator.hasNext()) {
                    neighbourItem = iterator.next();
                }
                break;
        }
        return neighbourItem;
    }

    protected void sortTableDsByItemsOrderNum() {
        DataUnit dataUnit = target.getItems();
        if (dataUnit instanceof ContainerDataUnit) {
            ContainerDataUnit containerDataUnit = (ContainerDataUnit) dataUnit;
            CollectionContainer<? extends OrderableEntity> collectionContainer = containerDataUnit.getContainer();
            List sortedItems = collectionContainer.getItems()
                    .stream()
                    .sorted(Comparator.comparingLong(OrderableEntity::getOrderNum))
                    .collect(Collectors.toList());
            collectionContainer.setItems(sortedItems);
        }
    }

    /**
     * Move few items up or down by recalculating their indexes.
     * Then we sorting table and normalize indexes
     */
    protected void moveFewItems() {
        //System.out.println("swap-------------");
        List<E> allItems = getItems();
        Set<E> selectedItems = target.getSelected();
        int spreadKoef = target.getSelected().size();//U can use 10 for easier debug

        long idx = 0;
        long lastIdxInGrp = Long.MAX_VALUE;
        long idxInGrp = 0;

        Map<OrderableEntity, Long> itemAndIndexInSelectedGroup = Collections.emptyMap();//for detection new orderNum values we must to store information about size of selected groups of items
        for (OrderableEntity item : allItems) {
            ++idx;
            item.setOrderNum(item.getOrderNum() * spreadKoef); //spread item indexes

            if (selectedItems.contains(item)) {
                if (itemAndIndexInSelectedGroup.isEmpty()) {
                    //start to store selected items with them index changing values to that map
                    itemAndIndexInSelectedGroup = new LinkedHashMap<>();
                }

                if (!itemAndIndexInSelectedGroup.isEmpty() || lastIdxInGrp == Long.MAX_VALUE) { //check that we are still in group of sequential selected items. sequence can contain one element
                    //we are enter inside group of selected item(s) now
                    idxInGrp++;
                    itemAndIndexInSelectedGroup.put(item, idxInGrp);
                }
                lastIdxInGrp = idx;
            } else {
                //we left group of sequential selected items. Now lets calc new orderNum values
                if (!itemAndIndexInSelectedGroup.isEmpty()) {
                    //System.out.print("*midGrp size" + idxInGrp + "*");
                    updateItemOrderNums(idxInGrp, itemAndIndexInSelectedGroup, spreadKoef);
                    itemAndIndexInSelectedGroup = Collections.emptyMap();
                }
                //reset counter values for re-use
                idxInGrp = 0;
                lastIdxInGrp = Long.MAX_VALUE;
            }
            //System.out.print("before " + item.getOrderNum() + " |");
            //System.out.print("after " + item.getOrderNum() + " |\n");
        }
        //we left group of sequential selected items. Last item was selected in list. Now lets calc new orderNum values
        if (!itemAndIndexInSelectedGroup.isEmpty()) {
            //System.out.print("*endGrp size" + idxInGrp + "*");
            updateItemOrderNums(idxInGrp, itemAndIndexInSelectedGroup, spreadKoef);
        }
        sortTableDsByItemsOrderNum();//lets sort by the new values
        normalizeEntityOrderNum(); //lets normalize recalculated indexes like 1,2,3...
    }

    private void updateItemOrderNums(long grpSize, Map<OrderableEntity, Long> itemAndIndexInGroup, int spreadKoef) {
        for (Map.Entry<OrderableEntity, Long> entityLongEntry: itemAndIndexInGroup.entrySet()) {
            OrderableEntity itemToChange = entityLongEntry.getKey();
            //System.out.print("*** before " + itemToChange.getOrderNum() + " |");
            long newValue = itemToChange.getOrderNum();
            long itemIndexInGrp = entityLongEntry.getValue();
            switch (direction) {
                case UP:
                    newValue = itemToChange.getOrderNum() + (grpSize - itemIndexInGrp) * spreadKoef - (grpSize - itemIndexInGrp + 1) - grpSize * spreadKoef;
                    break;
                case DOWN:
                    newValue = itemToChange.getOrderNum() - (itemIndexInGrp - 1) * spreadKoef + itemIndexInGrp + grpSize * spreadKoef;
                    break;
            }
            itemToChange.setOrderNum(newValue);
            //System.out.print("after " + itemToChange.getOrderNum() + " |\n");

        }
    }

    @Override
    public String getCaption() {
        return "";
    }

    private List getItems() {
        ContainerDataUnit<E> containerDataUnit = (ContainerDataUnit) target.getItems();
        return containerDataUnit.getContainer().getItems();
    }

    /**
     * Iterate over items and set orderNum value from 1 to size()+1;
     */
    protected void normalizeEntityOrderNum() {
        long normalizedIdx = 0;

        List<OrderableEntity> allItems = getItems();
        for (OrderableEntity item : allItems) {
            item.setOrderNum(++normalizedIdx); //first must to be 1
        }
    }

    public enum Direction {
        UP, DOWN
    }
}

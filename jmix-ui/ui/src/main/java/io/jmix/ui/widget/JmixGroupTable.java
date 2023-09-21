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

import com.google.common.collect.Sets;
import com.vaadin.event.Action;
import com.vaadin.server.KeyMapper;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.util.IndexedContainer;
import io.jmix.ui.widget.data.AggregationContainer;
import io.jmix.ui.widget.data.GroupTableContainer;
import io.jmix.ui.widget.data.util.NullGroupTableContainer;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class JmixGroupTable extends JmixTable implements GroupTableContainer {

    protected KeyMapper groupIdMap = new KeyMapper();

    protected Map<Object, List<String>> cachedAggregatedValues;

    protected List<Object> groupDisallowedProperties;

    protected GroupPropertyValueFormatter groupPropertyValueFormatter;

    protected boolean fixedGrouping = false;

    protected boolean requestColumnReorderingAllowed = true;

    protected boolean shouldPaintWithAggregations = true;

    protected String focusGroupAggregationInputColumnKey;

    protected boolean sortOnGroupEnabled = true;

    protected Map<Object, SortDetails> groupingPropertyIdsToSort = new LinkedHashMap<>();

    /**
     * Attention: this method is copied from the parent class: Table.setColumnOrder(Object[])
     */
    public void setColumnOrder(@Nullable Object[] columnOrder) {
        if (columnOrder == null || !isColumnReorderingAllowed()) {
            return;
        }
        List<Object> newOrder = new ArrayList<>();
        for (Object aColumnOrder : columnOrder) {
            if (aColumnOrder != null && _visibleColumns().contains(aColumnOrder)) {
                _visibleColumns().remove(aColumnOrder);
                newOrder.add(aColumnOrder);
            }
        }
        for (final Object columnId : _visibleColumns()) {
            if (!newOrder.contains(columnId)) {
                newOrder.add(columnId);
            }
        }
        _visibleColumns().clear();
        _visibleColumns().addAll(newOrder);

        // Assure visual refresh
        refreshRowCache();
    }

    @Override
    public void setContainerDataSource(Container newDataSource) {
        if (newDataSource == null || newDataSource instanceof IndexedContainer) { // if it is just created
            newDataSource = new NullGroupTableContainer(new IndexedContainer());
        } else if (!(newDataSource instanceof GroupTableContainer)) {
            throw new IllegalArgumentException("JmixGroupTable supports only GroupTableContainer");
        }

        super.setContainerDataSource(newDataSource);
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);

        if (hasGroups()) {
            Collection groupProperties = getGroupProperties();
            String[] groupColumns = new String[groupProperties.size()];

            int index = 0;
            for (Object groupColumnId : groupProperties) {
                groupColumns[index++] = _columnIdMap().key(groupColumnId);
            }
            target.addVariable(this, "groupColumns", groupColumns);
        }
    }

    @Override
    protected void paintAdditionalData(PaintTarget target) throws PaintException {
        super.paintAdditionalData(target);

        boolean hasAggregation = items instanceof AggregationContainer && isAggregatable()
                && !((AggregationContainer) items).getAggregationPropertyIds().isEmpty();

        // first call, we shouldn't update aggregation group rows
        if (cachedAggregatedValues == null) {
            cachedAggregatedValues = new HashMap<>();
            // fill with initial values
            if (hasAggregation) {
                for (Object itemId : getVisibleItemIds()) {
                    if (isGroup(itemId)) {
                        cachedAggregatedValues.put(itemId, getAggregatedValuesForGroup(itemId));
                    }
                }
            }
            return;
        }

        boolean cacheIsEmpty = cachedAggregatedValues.isEmpty();
        boolean isAddedToCache = false;

        if (hasGroups() && hasAggregation) {
            target.startTag("groupRows");
            for (Object itemId : getVisibleItemIds()) {
                if (isGroup(itemId) && isAggregatedValuesChanged(itemId)) {
                    target.startTag("tr");

                    target.addAttribute("groupKey", groupIdMap.key(itemId));
                    paintUpdatesForGroupRowWithAggregation(target, itemId);

                    target.endTag("tr");

                    isAddedToCache = true;
                }
            }

            paintEditableAggregationColumns(target);

            target.endTag("groupRows");
        }

        // if cachedAggregatedValues is empty, so rendered cells was refreshed
        // and we need to paint visible columns and actions
        shouldPaintWithAggregations = cacheIsEmpty || !isAddedToCache;
    }

    @Override
    protected void paintVisibleColumns(PaintTarget target) throws PaintException {
        if (shouldPaintWithAggregations) {
            super.paintVisibleColumns(target);
        }
    }

    @Override
    protected void paintActions(PaintTarget target, Set<Action> actionSet) throws PaintException {
        if (shouldPaintWithAggregations) {
            super.paintActions(target, actionSet);
        }
    }

    protected void paintUpdatesForGroupRowWithAggregation(PaintTarget target, Object groupId) throws PaintException {
        target.startTag("updateAggregation");
        List<String> values = getAggregatedValuesForGroup(groupId);
        for (String value : values) {
            target.addText(value);
        }
        target.endTag("updateAggregation");

        cachedAggregatedValues.put(groupId, values);
    }

    protected boolean isAggregatedValuesChanged(@Nullable Object itemId) {
        if (itemId == null) {
            return false;
        }

        List<String> cachedValues = cachedAggregatedValues.get(itemId);
        if (cachedValues == null) {
            return true;
        }

        List<String> aggregatedValues = getAggregatedValuesForGroup(itemId);
        if (cachedValues.size() != aggregatedValues.size()) {
            return true;
        }

        for (int i = 0; i < cachedValues.size(); i++) {
            if (!Objects.equals(cachedValues.get(i), aggregatedValues.get(i))) {
                return true;
            }
        }

        return false;
    }

    protected List<String> getAggregatedValuesForGroup(Object itemId) {
        List<String> values = new ArrayList<>();

        Map<Object, Object> aggregations = ((AggregationContainer) items).aggregate(
                new GroupAggregationContext(this, itemId));

        boolean paintGroupProperty = false;
        final Collection groupProperties = getGroupProperties();
        final Object groupProperty = getGroupProperty(itemId);
        for (final Object columnId : getVisibleColumns()) {
            if (columnId == null || isColumnCollapsed(columnId)) {
                continue;
            }
            if (groupProperties.contains(columnId) && !paintGroupProperty) {
                if (columnId.equals(groupProperty)) {
                    paintGroupProperty = true;
                }
                continue;
            }

            String value = (String) aggregations.get(columnId);
            if (value != null) {
                values.add(value);
            } else {
                values.add("");
            }
        }
        return values;
    }

    @Override
    public boolean isColumnReorderingAllowed() {
        return requestColumnReorderingAllowed && super.isColumnReorderingAllowed();
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        Object[] newGroupProperties = null;

        if (variables.containsKey("columnorder") && !variables.containsKey("groupedcolumns")) {
            newGroupProperties = new Object[0];
        } else if (variables.containsKey("groupedcolumns")) {
            focus();

            Object[] ids = (Object[]) variables.get("groupedcolumns");
            Object[] groupProperties = new Object[ids.length];
            for (int i = 0; i < ids.length; i++) {
                groupProperties[i] = _columnIdMap().get(ids[i].toString());
            }
            newGroupProperties = groupProperties;
            // Deny group by generated columns
            if (!_columnGenerators().isEmpty()) {
                List<Object> notGeneratedProperties = new ArrayList<>();
                for (Object id : newGroupProperties) {
                    // todo support grouping by generated columns with Printable
                    if (!_columnGenerators().containsKey(id) || isNonGeneratedProperty(id)) {
                        notGeneratedProperties.add(id);
                    }
                }
                newGroupProperties = notGeneratedProperties.toArray();
            }
        }

        if (variables.containsKey("collapsedcolumns")) {
            Object[] ids = (Object[]) variables.get("collapsedcolumns");

            Set<Object> idSet = ids.length > 0 ? new HashSet<>() : Collections.emptySet();

            for (Object id : ids) {
                idSet.add(_columnIdMap().get(id.toString()));
            }

            boolean needToRegroup = false;
            List<Object> groupProperties = new ArrayList<>(getGroupProperties());
            for (int index = 0; index < groupProperties.size(); index++) {
                final Object propertyId = groupProperties.get(index);
                if (idSet.contains(propertyId)) {
                    groupProperties.subList(index, groupProperties.size()).clear();
                    needToRegroup = true;
                    break;
                }
            }
            if (needToRegroup) {
                newGroupProperties = groupProperties.toArray();
            }
        }

        if ((hasGroupDisallowedProperties(newGroupProperties) || fixedGrouping)
                && isGroupsChanged(newGroupProperties)) {
            requestColumnReorderingAllowed = false;
            markAsDirty();
        }

        super.changeVariables(source, variables);

        if (!(hasGroupDisallowedProperties(newGroupProperties) || fixedGrouping)
                && newGroupProperties != null && isGroupsChanged(newGroupProperties)) {
            groupBy(newGroupProperties, true);
        }

        requestColumnReorderingAllowed = true;
    }

    protected boolean hasGroupDisallowedProperties(@Nullable Object[] newGroupProperties) {
        if (newGroupProperties == null) {
            return false;
        }

        if (CollectionUtils.isEmpty(groupDisallowedProperties)) {
            return false;
        }

        for (Object property : newGroupProperties) {
            if (groupDisallowedProperties.contains(property)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isGroupsChanged(@Nullable Object[] newGroupProperties) {
        Collection<?> oldGroupProperties = getGroupProperties();
        if (newGroupProperties == null && oldGroupProperties == null)
            return false;

        if (newGroupProperties == null)
            return true;

        if (oldGroupProperties == null)
            return true;

        if (oldGroupProperties.size() != newGroupProperties.length)
            return true;

        int i = 0;
        for (Object oldGroupProperty : oldGroupProperties) {
            if (!Objects.equals(oldGroupProperty, newGroupProperties[i]))
                return true;
            i++;
        }

        return false;
    }

    @Override
    protected boolean changeVariables(Map<String, Object> variables) {
        boolean clientNeedsContentRefresh = super.changeVariables(variables);

        boolean needsResetPageBuffer = false;

        if (variables.containsKey("expandAllInGroup")) {
            focus();

            Object groupId = groupIdMap.get((String) variables.get("expandAllInGroup"));
            expandAllInGroup(groupId, false);
            clientNeedsContentRefresh = true;
            needsResetPageBuffer = true;
        }

        if (variables.containsKey("expand")) {
            focus();

            Object groupId = groupIdMap.get((String) variables.get("expand"));
            expand(groupId, false);
            clientNeedsContentRefresh = true;
            needsResetPageBuffer = true;
        }

        if (variables.containsKey("collapse")) {
            focus();

            Object groupId = groupIdMap.get((String) variables.get("collapse"));
            collapse(groupId, false);
            clientNeedsContentRefresh = true;
            needsResetPageBuffer = true;
        }

        if (needsResetPageBuffer) {
            resetPageBuffer();
        }

        return clientNeedsContentRefresh;
    }

    @Override
    protected boolean isCellPaintingNeeded(Object itemId, Object columnId) {
        return !isGroup(itemId);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void paintRowAttributes(PaintTarget target, Object itemId) throws PaintException {
        super.paintRowAttributes(target, itemId);

        boolean hasAggregation = items instanceof AggregationContainer && isAggregatable()
                && !((AggregationContainer) items).getAggregationPropertyIds().isEmpty();

        boolean hasGroups = hasGroups();
        if (hasGroups) {
            if (isGroup(itemId)) {
                target.addAttribute("colKey", _columnIdMap().key(getGroupProperty(itemId)));
                target.addAttribute("groupKey", groupIdMap.key(itemId));

                if (isExpanded(itemId)) {
                    target.addAttribute("expanded", true);
                }

                Object propertyValue = getGroupPropertyValue(itemId);
                String formattedValue = formatGroupPropertyValue(itemId, propertyValue);
                target.addAttribute("groupCaption", formattedValue);

                if (hasAggregation) {
                    paintGroupAggregation(target, itemId,
                            ((AggregationContainer) items).aggregate(new GroupAggregationContext(this, itemId)));

                    paintEditableAggregationColumns(target);

                    if (focusGroupAggregationInputColumnKey != null) {
                        target.addAttribute("focusInput", focusGroupAggregationInputColumnKey);
                        focusGroupAggregationInputColumnKey = null;
                    }
                }
            }
        }
    }

    @Override
    protected Collection<?> getAggregationItemIds() {
        if (hasGroups()) {
            return rootGroups().stream()
                    .flatMap(groupId -> getGroupItemIds(groupId).stream())
                    .collect(Collectors.toList());
        } else {
            return items.getItemIds();
        }
    }

    protected void paintGroupAggregation(PaintTarget target, Object groupId, Map<Object, Object> aggregations)
            throws PaintException {
        boolean paintGroupProperty = false;

        Collection groupProperties = getGroupProperties();
        Object groupProperty = getGroupProperty(groupId);

        for (Object columnId : _visibleColumns()) {
            if (columnId == null || isColumnCollapsed(columnId)) {
                continue;
            }

            if (groupProperties.contains(columnId) && !paintGroupProperty) {
                if (columnId.equals(groupProperty)) {
                    paintGroupProperty = true;
                }
                continue;
            }

            if (getCellStyleGenerator() != null) {
                String cellStyle = getCellStyleGenerator().getStyle(this, null, columnId);
                if (cellStyle != null && !cellStyle.isEmpty()) {
                    target.addAttribute("style-" + _columnIdMap().key(columnId), cellStyle + "-ag");
                }
            }

            String value = (String) aggregations.get(columnId);
            if (value != null) {
                target.addText(value);
            } else {
                target.addText("");
            }
        }
    }

    @Override
    protected LinkedHashSet<Object> getItemIdsInRange(Object startItemId, final int length) {
        Set<Object> rootIds = super.getItemIdsInRange(startItemId, length);
        // actual implementation moved to WebGroupTable
        return new LinkedHashSet<>(rootIds);
    }

    @Override
    protected boolean isColumnNeedsToRefreshRendered(Object colId) {
        GroupTableContainer items = (GroupTableContainer) this.items;
        boolean groupped = items.hasGroups();

        return !groupped || !getGroupProperties().contains(colId);
    }

    @Override
    protected boolean isItemNeedsToRefreshRendered(Object itemId) {
        GroupTableContainer items = (GroupTableContainer) this.items;
        boolean groupped = items.hasGroups();

        return !groupped || !items.isGroup(itemId);
    }

    protected String formatGroupPropertyValue(Object groupId, @Nullable Object groupValue) {
        return groupPropertyValueFormatter != null
                ? groupPropertyValueFormatter.format(groupId, groupValue)
                : (groupValue == null ? "" : groupValue.toString());
    }

    protected void expandAllInGroup(Object id, boolean rerender) {
        int pageIndex = getCurrentPageFirstItemIndex();
        expandAllInGroup(id);
        if (isMultiSelect()) {
            selectAllInGroup(id);
        }
        setCurrentPageFirstItemIndex(pageIndex, false);
        if (rerender) {
            resetPageBuffer();
            refreshRenderedCells();
            markAsDirty();
        }
    }

    protected void expandAllInGroup(Object id) {
        ((GroupTableContainer) items).expand(id);
        if (hasChildren(id)) {
            for (Object childId : getChildren(id)) {
                expandAllInGroup(childId);
            }
        }
    }

    protected void selectAllInGroup(Object id) {
        for (Object itemId : getGroupItemIds(id)) {
            select(itemId);
        }
    }

    protected void expand(Object id, boolean rerender) {
        int pageIndex = getCurrentPageFirstItemIndex();
        ((GroupTableContainer) items).expand(id);
        setCurrentPageFirstItemIndex(pageIndex, false);
        if (rerender) {
            resetPageBuffer();
            refreshRenderedCells();
            markAsDirty();
        }
    }

    protected void collapse(Object id, boolean rerender) {
        int pageIndex = getCurrentPageFirstItemIndex();
        ((GroupTableContainer) items).collapse(id);
        setCurrentPageFirstItemIndex(pageIndex, false);
        if (rerender) {
            resetPageBuffer();
            refreshRenderedCells();
            markAsDirty();
        }
    }

    protected void groupBy(Object[] properties, boolean rerender) {
        GroupTableContainer groupTableContainer = (GroupTableContainer) items;
        if (groupTableContainer.getGroupProperties().isEmpty() && properties.length == 0) {
            // no need to regroup and refreshRenderedCells
            return;
        }

        groupTableContainer.groupBy(properties);
        if (rerender) {
            resetPageBuffer();
            setCurrentPageFirstItemIndex(0, false);
            refreshRenderedCells();
            markAsDirty();
        }
        if (sortOnGroupEnabled) {
            sortByGroupingProperties(properties);
        }
    }

    protected void sortByGroupingProperties(Object[] groupingPropertyIds) {
        Map<Object, SortDetails> newPropertyIdsToSort = new LinkedHashMap<>();
        if (isSortEnabled()) {
            for (Object property : groupingPropertyIds) {
                SortDetails sortDetails = groupingPropertyIdsToSort.getOrDefault(property,
                        new SortDetails(true, false));
                newPropertyIdsToSort.put(property, sortDetails);
            }
            groupingPropertyIdsToSort = newPropertyIdsToSort;

            if (sortContainerPropertyId != null) {
                sort(new Object[]{sortContainerPropertyId}, new boolean[]{sortAscending});
            } else {
                sort(new Object[0], new boolean[0]);
            }
        }
    }

    @Override
    public void sort(Object[] propertyIds, boolean[] ascendingValues) throws UnsupportedOperationException {
        if (sortOnGroupEnabled) {
            Container containerDataSource = getContainerDataSource();
            if (containerDataSource instanceof Container.Sortable) {
                Map<Object, SortDetails> allSortPropertyIds = getAllPropertyIdsToSort(propertyIds, ascendingValues);

                int pageIndex = getCurrentPageFirstItemIndex();
                boolean refreshingPreviouslyEnabled = disableContentRefreshing();
                sortByDataSource(allSortPropertyIds, ((Container.Sortable) containerDataSource));
                setCurrentPageFirstItemIndex(pageIndex);
                if (refreshingPreviouslyEnabled) {
                    enableContentRefreshing(true);
                }
                refreshGroupingPropertyUserOriginated(propertyIds);
                updateCurrentSortInfo(allSortPropertyIds);
            } else if (containerDataSource != null) {
                throw new UnsupportedOperationException(
                        "Underlying Data does not allow sorting");
            }
        } else {
            super.sort(propertyIds, ascendingValues);
        }
    }

    /**
     * Gets all property ids to sort including automatically sorted grouping properties and properties
     * directly set to sort
     *
     * @param propertyIds     property ids that were directly set to sort
     * @param ascendingValues ascending values of properties that were directly set to sort
     * @return property ids to sort which include automatically sorted grouping properties and properties
     * directly set to sort
     */
    protected Map<Object, SortDetails> getAllPropertyIdsToSort(Object[] propertyIds, boolean[] ascendingValues) {
        Map<Object, SortDetails> allPropertyIdsToSort = new LinkedHashMap<>(groupingPropertyIdsToSort);
        for (int i = 0; i < propertyIds.length; i++) {
            Object propertyId = propertyIds[i];
            SortDetails sortDetails = allPropertyIdsToSort.computeIfAbsent(propertyId, pid -> new SortDetails());
            sortDetails.setAscending(ascendingValues[i]);
            sortDetails.setUserOriginated(true);
        }
        return allPropertyIdsToSort;
    }

    protected void refreshGroupingPropertyUserOriginated(Object[] currentPropertyIdsToSort) {
        Set<Object> propertyIdsSet = Sets.newHashSet(currentPropertyIdsToSort);

        for (Map.Entry<Object, SortDetails> propertySortDetailsEntry : groupingPropertyIdsToSort.entrySet()) {
            Object propertyId = propertySortDetailsEntry.getKey();
            if (!propertyIdsSet.contains(propertyId)) {
                propertySortDetailsEntry.getValue().setUserOriginated(false);
            }
        }
    }

    protected void sortByDataSource(Map<Object, SortDetails> sortPropertyIds, Container.Sortable sortableContainer) {
        Object[] allPropertyIds = new Object[sortPropertyIds.size()];
        boolean[] allAscendingValues = new boolean[sortPropertyIds.size()];
        int i = 0;
        for (Map.Entry<Object, SortDetails> propertyIdSortDetailEntry : sortPropertyIds.entrySet()) {
            allPropertyIds[i] = propertyIdSortDetailEntry.getKey();
            allAscendingValues[i] = propertyIdSortDetailEntry.getValue().isAscending();
            i++;
        }
        try {
            sortableContainer.sort(allPropertyIds, allAscendingValues);
        } catch (Exception e) {
            enableContentRefreshing(false);
            throw e;
        }
    }

    protected void updateCurrentSortInfo(Map<Object, SortDetails> sortedProperties) {
        sortedProperties.entrySet().stream()
                .filter(entry -> entry.getValue().isUserOriginated())
                .findFirst()
                .ifPresentOrElse(
                        entry -> {
                            sortAscending = entry.getValue().isAscending();
                            sortContainerPropertyId = entry.getKey();
                        },
                        () -> {
                            sortAscending = false;
                            sortContainerPropertyId = null;
                        }
                );
    }

    @Override
    public void resetSortOrder() {
        if (!sortOnGroupEnabled || groupingPropertyIdsToSort.isEmpty()) {
            super.resetSortOrder();
        } else {
            for (SortDetails sortDetails : groupingPropertyIdsToSort.values()) {
                sortDetails.setAscending(true);
                sortDetails.setUserOriginated(false);
            }
            sort(new Object[0], new boolean[0]);
        }
    }

    // hook to implement in Web impl
    protected boolean isNonGeneratedProperty(Object id) {
        return false;
    }

    @Override
    public Collection<?> getGroupProperties() {
        Collection<?> groupProperties = ((GroupTableContainer) items).getGroupProperties();
        // Deny group by generated columns
        if (!_columnGenerators().isEmpty()) {
            List<Object> notGeneratedGroupProps = new ArrayList<>();
            for (Object id : groupProperties) {
                if (!_columnGenerators().containsKey(id) || isNonGeneratedProperty(id)) {
                    notGeneratedGroupProps.add(id);
                }
            }
            return notGeneratedGroupProps;
        } else {
            return groupProperties;
        }
    }

    @Override
    public void expandAll() {
        int pageIndex = getCurrentPageFirstItemIndex();
        ((GroupTableContainer) items).expandAll();
        setCurrentPageFirstItemIndex(pageIndex, false);
        resetPageBuffer();
        refreshRenderedCells();
        markAsDirty();
    }

    @Override
    public void expand(Object id) {
        expand(id, true);
    }

    @Override
    public void collapseAll() {
        int pageIndex = getCurrentPageFirstItemIndex();
        ((GroupTableContainer) items).collapseAll();
        setCurrentPageFirstItemIndex(pageIndex, false);
        resetPageBuffer();
        refreshRenderedCells();
        markAsDirty();
    }

    @Override
    public void collapse(Object id) {
        collapse(id, true);
    }

    @Override
    public boolean hasGroups() {
        return ((GroupTableContainer) items).hasGroups();
    }

    @Override
    public void groupBy(Object[] properties) {
        groupBy(properties, true);
    }

    public boolean getColumnGroupAllowed(Object id) {
        if (groupDisallowedProperties == null) {
            return true;
        }
        return !groupDisallowedProperties.contains(id);
    }

    public void setColumnGroupAllowed(Object id, boolean allowed) {
        if (groupDisallowedProperties == null) {
            groupDisallowedProperties = new ArrayList<>();
        }
        if (allowed) {
            groupDisallowedProperties.remove(id);
        } else {
            groupDisallowedProperties.add(id);
        }
    }

    @Override
    public boolean isGroup(Object itemId) {
        return ((GroupTableContainer) items).isGroup(itemId);
    }

    @Override
    public Collection<?> rootGroups() {
        return ((GroupTableContainer) items).rootGroups();
    }

    @Override
    public boolean hasChildren(Object id) {
        return ((GroupTableContainer) items).hasChildren(id);
    }

    @Override
    public Collection<?> getChildren(Object id) {
        return ((GroupTableContainer) items).getChildren(id);
    }

    @Nullable
    @Override
    public Object getGroupProperty(Object itemId) {
        return ((GroupTableContainer) items).getGroupProperty(itemId);
    }

    @Nullable
    @Override
    public Object getGroupPropertyValue(Object itemId) {
        return ((GroupTableContainer) items).getGroupPropertyValue(itemId);
    }

    @Override
    public Collection<?> getGroupItemIds(Object itemId) {
        return ((GroupTableContainer) items).getGroupItemIds(itemId);
    }

    @Override
    public int getGroupItemsCount(Object itemId) {
        return ((GroupTableContainer) items).getGroupItemsCount(itemId);
    }

    @Override
    public boolean isExpanded(Object id) {
        return ((GroupTableContainer) items).isExpanded(id);
    }

    public boolean isFixedGrouping() {
        return fixedGrouping;
    }

    public void setFixedGrouping(boolean fixedGrouping) {
        this.fixedGrouping = fixedGrouping;
        markAsDirty();
    }

    @Override
    protected void handleAggregationGroupInputChange(String columnKey, String groupKey, String value, boolean isFocused) {
        if (aggregationDistributionProvider != null) {
            Object columnId = _columnIdMap().get(columnKey);
            Object groupColumnId = groupIdMap.get(groupKey);

            focusGroupAggregationInputColumnKey = isFocused ? columnKey : null;

            GroupAggregationInputValueChangeContext context
                    = new GroupAggregationInputValueChangeContext(columnId, value, false, groupColumnId);
            if (!aggregationDistributionProvider.apply(context)) {
                // clear cache to update aggregated values
                cachedAggregatedValues.clear();
                markAsDirty();
            }
        }
    }

    @Override
    public void setSortOptions(Object propertyId, boolean sortAscending) {
        super.setContainerSortOptions(propertyId, sortAscending);
    }

    @Override
    protected void refreshRenderedCells() {
        if (cachedAggregatedValues != null) {
            cachedAggregatedValues.clear();
        }
        super.refreshRenderedCells();
    }

    public GroupPropertyValueFormatter getGroupPropertyValueFormatter() {
        return groupPropertyValueFormatter;
    }

    public void setGroupPropertyValueFormatter(GroupPropertyValueFormatter groupPropertyValueFormatter) {
        this.groupPropertyValueFormatter = groupPropertyValueFormatter;
    }

    public interface GroupPropertyValueFormatter {
        String format(Object groupId, @Nullable Object value);
    }

    public boolean isSortOnGroupEnabled() {
        return sortOnGroupEnabled;
    }

    public void setSortOnGroupEnabled(boolean sortOnGroupEnabled) {
        this.sortOnGroupEnabled = sortOnGroupEnabled;
    }

    public static class GroupAggregationContext extends Context {
        private Object groupId;

        public GroupAggregationContext(GroupTableContainer datasource, Object groupId) {
            super(datasource.getGroupItemIds(groupId));
            this.groupId = groupId;
        }

        public Object getGroupId() {
            return groupId;
        }
    }

    public static class GroupAggregationInputValueChangeContext extends AggregationInputValueChangeContext {
        protected Object groupInfo;

        public GroupAggregationInputValueChangeContext(Object columnId, String value, boolean isTotalAggregation,
                                                       Object groupInfo) {
            super(columnId, value, isTotalAggregation);
            this.groupInfo = groupInfo;
        }

        public Object getGroupInfo() {
            return groupInfo;
        }
    }


    protected static class SortDetails {
        protected boolean ascending;
        protected boolean userOriginated;

        protected SortDetails() {
        }

        protected SortDetails(boolean ascending, boolean userOriginated) {
            this.ascending = ascending;
            this.userOriginated = userOriginated;
        }

        protected boolean isAscending() {
            return ascending;
        }

        protected void setAscending(boolean ascending) {
            this.ascending = ascending;
        }

        protected boolean isUserOriginated() {
            return userOriginated;
        }

        protected void setUserOriginated(boolean userOriginated) {
            this.userOriginated = userOriginated;
        }
    }
}

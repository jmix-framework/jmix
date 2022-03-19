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

package io.jmix.ui.component.data.table;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.component.data.GroupInfo;
import io.jmix.ui.component.data.GroupTableItems;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EmptyGroupTableItems<E> extends EmptyTableItems<E> implements GroupTableItems<E> {

    public EmptyGroupTableItems(MetaClass metaClass) {
        super(metaClass);
    }

    @Override
    public void groupBy(@Nullable Object[] properties) {
        // do nothing
    }

    @Override
    public List<GroupInfo> rootGroups() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasChildren(GroupInfo groupId) {
        return false;
    }

    @Override
    public List<GroupInfo> getChildren(GroupInfo groupId) {
        return Collections.emptyList();
    }

    @Override
    public List<E> getOwnChildItems(GroupInfo groupId) {
        return Collections.emptyList();
    }

    @Override
    public List<E> getChildItems(GroupInfo groupId) {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public GroupInfo getParentGroup(E item) {
        return null;
    }

    @Override
    public List<GroupInfo> getGroupPath(E item) {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public Object getGroupProperty(GroupInfo groupId) {
        return null;
    }

    @Nullable
    @Override
    public Object getGroupPropertyValue(GroupInfo groupId) {
        return null;
    }

    @Override
    public Collection<?> getGroupItemIds(GroupInfo groupId) {
        return Collections.emptyList();
    }

    @Override
    public int getGroupItemsCount(GroupInfo groupId) {
        return 0;
    }

    @Override
    public boolean hasGroups() {
        return false;
    }

    @Override
    public Collection<?> getGroupProperties() {
        return Collections.emptyList();
    }

    @Override
    public boolean containsGroup(GroupInfo groupId) {
        return false;
    }

    @Nullable
    @Override
    public Object nextItemId(@Nullable Object itemId) {
        return null;
    }

    @Nullable
    @Override
    public Object prevItemId(@Nullable Object itemId) {
        return null;
    }

    @Nullable
    @Override
    public Object firstItemId() {
        return null;
    }

    @Nullable
    @Override
    public Object lastItemId() {
        return null;
    }

    @Override
    public boolean isFirstId(@Nullable Object itemId) {
        return false;
    }

    @Override
    public boolean isLastId(@Nullable Object itemId) {
        return false;
    }

    @Override
    public void sort(Object[] propertyId, boolean[] ascending) {
        // do nothing
    }

    @Override
    public void resetSortOrder() {
        // do nothing
    }
}

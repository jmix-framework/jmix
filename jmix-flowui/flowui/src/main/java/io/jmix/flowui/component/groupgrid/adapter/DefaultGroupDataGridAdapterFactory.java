/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.component.groupgrid.adapter;

import io.jmix.flowui.component.groupgrid.AbstractGroupDataGridAdapter;
import io.jmix.flowui.component.groupgrid.GroupGrid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.List;

public class DefaultGroupDataGridAdapterFactory implements GroupDataGridAdapterFactory {

    protected List<GroupDataGridAdapterProvider> adapterProviders;

    public DefaultGroupDataGridAdapterFactory(@Autowired(required = false)
                                              List<GroupDataGridAdapterProvider> adapterProviders) {
        this.adapterProviders = adapterProviders;
    }

    @Nullable
    @Override
    public <E> AbstractGroupDataGridAdapter<E> getAdapter(GroupGrid<E> groupGrid) {
        if (adapterProviders == null) {
            return null;
        }

        for (GroupDataGridAdapterProvider adapterProvider : adapterProviders) {
            AbstractGroupDataGridAdapter<E> adapter = adapterProvider.getAdapter(groupGrid);
            if (adapter != null) {
                return adapter;
            }
        }
        return null;
    }
}

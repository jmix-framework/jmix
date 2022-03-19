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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.components.LookupComponent;
import com.haulmont.cuba.gui.components.Tree;
import com.haulmont.cuba.gui.components.data.tree.DatasourceTreeItems;
import io.jmix.core.Entity;
import io.jmix.datatoolsui.accesscontext.UiShowEntityInfoContext;
import io.jmix.datatoolsui.action.ShowEntityInfoAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.data.TreeItems;
import io.jmix.ui.component.impl.TreeImpl;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

@Deprecated
public class WebTree<E extends Entity>
        extends TreeImpl<E>
        implements Tree<E>, LookupComponent.LookupSelectionChangeNotifier<E> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void removeLookupValueChangeListener(Consumer<LookupSelectionChangeEvent<E>> listener) {
        unsubscribe(LookupSelectionChangeEvent.class, (Consumer) listener);
    }

    @Override
    public void setMultiSelect(boolean multiselect) {
        setSelectionMode(multiselect
                ? SelectionMode.MULTI
                : SelectionMode.SINGLE);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void refresh() {
        TreeItems<E> treeItems = getItems();

        if (treeItems instanceof DatasourceTreeItems) {
            ((DatasourceTreeItems) treeItems).getDatasource().refresh();
        }
    }

    @Override
    public void setItems(@Nullable TreeItems<E> treeItems) {
        super.setItems(treeItems);

        initShowEntityInfoAction();
    }

    protected void initShowEntityInfoAction() {
        UiShowEntityInfoContext showInfoContext = new UiShowEntityInfoContext();
        accessManager.applyRegisteredConstraints(showInfoContext);

        if (showInfoContext.isPermitted()) {
            if (getAction(ShowEntityInfoAction.ID) == null) {
                addAction(actions.create(ShowEntityInfoAction.ID));
            }
        }
    }

    @Override
    public void setDetailsGenerator(@Nullable DetailsGenerator<? super E> generator) {
        super.setDetailsGenerator((Function<E, Component>) generator);
    }
}

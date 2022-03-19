/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.gui.components.filter.addcondition;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.filter.FilterHelper;
import com.haulmont.cuba.gui.components.filter.descriptor.AbstractConditionDescriptor;
import com.haulmont.cuba.gui.components.filter.descriptor.HeaderConditionDescriptor;
import com.haulmont.cuba.gui.components.filter.descriptor.PropertyConditionDescriptor;
import io.jmix.core.Entity;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.component.*;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.theme.ThemeConstantsManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Window for adding new filter condition
 */
public class AddConditionWindow extends AbstractWindow {

    @Autowired
    protected ConditionDescriptorsDs conditionDescriptorsDs;

    @Autowired
    protected TextField<String> treeFilter;

    @Autowired
    protected Tree tree;

    @Autowired
    protected Button cancelBtn;

    @Autowired
    protected Button selectBtn;

    @Autowired
    protected ThemeConstantsManager themeConstantsManager;

    @Autowired
    protected Metadata metadata;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        ThemeConstants theme = themeConstantsManager.getConstants();

        getDialogOptions()
                .setHeight(theme.get("cuba.gui.addFilterCondition.dialog.height"))
                .setWidth(theme.get("cuba.gui.addFilterCondition.dialog.width"))
                .setResizable(true);

        conditionDescriptorsDs.refresh(params);
        expandTreeRoots();
        tree.setItemClickAction(new AbstractAction("select") {
            @Override
            public void actionPerform(Component component) {
                select();
            }
        });

        FilterHelper filterHelper = AppBeans.get(FilterHelper.class);
        filterHelper.addTextChangeListener(treeFilter, this::_search);

        filterHelper.addShortcutListener(treeFilter, new FilterHelper.ShortcutListener("search", new KeyCombination(KeyCombination.Key.ENTER)) {
            @Override
            public void handleShortcutPressed() {
                search();
            }
        });
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public void expandTreeRoots() {
        Collection<UUID> rootItemIds = conditionDescriptorsDs.getRootItemIds();
        for (UUID rootItemId : rootItemIds) {
            tree.expand(conditionDescriptorsDs.getItem(rootItemId));
        }
    }

    public void search() {
        String filterValue = treeFilter.getValue();
        _search(filterValue);
    }

    protected void _search(String filterValue) {
        conditionDescriptorsDs.setFilter(filterValue);
        tree.expandTree();
    }

    public void select() {
        Set<Entity> selectedItems = tree.getSelected();
        if (selectedItems.isEmpty()) {
            showNotification(messages.getMessage("filter.addCondition.selectCondition"), NotificationType.WARNING);
            return;
        } else {
            for (Entity item : selectedItems) {
                if (item instanceof HeaderConditionDescriptor) {
                    showNotification(messages.getMessage("filter.addCondition.youSelectedGroup"), NotificationType.WARNING);
                    return;
                } else if (isEmbeddedProperty((AbstractConditionDescriptor) item)) {
                    showNotification(messages.getMessage("filter.addCondition.youSelectedEmbedded"), NotificationType.WARNING);
                    return;
                }
            }
        }

        close(COMMIT_ACTION_ID);
    }

    protected boolean isEmbeddedProperty(AbstractConditionDescriptor item) {
        if (item instanceof PropertyConditionDescriptor) {
            MetaProperty metaProperty = ((PropertyConditionDescriptor) item).getMetaProperty();
            if (metaProperty != null && metadata.getTools().isEmbedded(metaProperty)) {
                return true;
            }
        }
        return false;
    }

    public void cancel() {
        close(CLOSE_ACTION_ID);
    }

    public Collection<AbstractConditionDescriptor> getDescriptors() {
        return tree.getSelected();
    }
}

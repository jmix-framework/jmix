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

package io.jmix.tabbedmode.action.tabsheet;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Messages;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.tabbedmode.TabbedModeProperties;
import io.jmix.tabbedmode.Views;
import io.jmix.tabbedmode.component.tabsheet.JmixViewTab;
import io.jmix.tabbedmode.component.workarea.TabbedViewsContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Closes all tabs.
 */
@ActionType(CloseAllTabsAction.ID)
public class CloseAllTabsAction extends AbstractCloseTabsAction<CloseAllTabsAction> {

    public static final String ID = "tabmod_closeAllTabs";

    protected Registration tabsCollectionChangeListener;

    public CloseAllTabsAction() {
        this(ID);
    }

    public CloseAllTabsAction(String id) {
        super(id);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.closeAllTabs.text");
    }

    @Autowired
    protected void setTabbedModeProperties(TabbedModeProperties properties) {
        this.shortcutCombination = KeyCombination.create(properties.getCloseAllTabsShortcut());
    }

    @Override
    protected void detachListeners(TabbedViewsContainer<?> target) {
        super.detachListeners(target);

        if (tabsCollectionChangeListener != null) {
            tabsCollectionChangeListener.remove();
            tabsCollectionChangeListener = null;
        }
    }

    @Override
    protected void attachListeners(TabbedViewsContainer<?> target) {
        super.attachListeners(target);

        tabsCollectionChangeListener = target.addTabsCollectionChangeListener(event ->
                refreshState());
    }

    @Override
    protected boolean hasCloseableTabs() {
        if (target.getTabsStream().findAny().isEmpty()) {
            return false;
        }

        return target.getTabsStream()
                .anyMatch(tab ->
                        tab instanceof JmixViewTab viewTab
                                && viewTab.isClosable()
                );
    }

    @Override
    public void execute(@Nullable Component trigger) {
        checkTarget();

        List<Views.ViewStack> viewStacks = target.getTabComponentsStream()
                .map(this::asViewStack)
                .toList();

        closeViewStacks(viewStacks);
    }
}

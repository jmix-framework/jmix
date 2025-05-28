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
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.Messages;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.tabbedmode.TabbedModeProperties;
import io.jmix.tabbedmode.Views;
import io.jmix.tabbedmode.component.tabsheet.ViewTab;
import io.jmix.tabbedmode.component.workarea.TabbedViewsContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Closes all tabs except the selected (trigger) tab.
 */
@ActionType(CloseOtherTabsAction.ID)
public class CloseOtherTabsAction extends AbstractCloseTabsAction<CloseOtherTabsAction> {

    private static final Logger log = LoggerFactory.getLogger(CloseOtherTabsAction.class);

    public static final String ID = "tabmod_closeOtherTabs";

    protected Registration tabsCollectionChangeListener;
    protected Registration contextMenuTargetListener;

    public CloseOtherTabsAction() {
        this(ID);
    }

    public CloseOtherTabsAction(String id) {
        super(id);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.closeOtherTabs.text");
    }

    @Autowired
    protected void setTabbedModeProperties(TabbedModeProperties properties) {
        this.shortcutCombination = KeyCombination.create(properties.getCloseOtherTabsShortcut());
    }

    @Override
    protected void detachListeners(TabbedViewsContainer<?> target) {
        super.detachListeners(target);

        if (tabsCollectionChangeListener != null) {
            tabsCollectionChangeListener.remove();
            tabsCollectionChangeListener = null;
        }

        if (contextMenuTargetListener != null) {
            contextMenuTargetListener.remove();
            contextMenuTargetListener = null;
        }
    }

    @Override
    protected void attachListeners(TabbedViewsContainer<?> target) {
        super.attachListeners(target);

        tabsCollectionChangeListener = target.addTabsCollectionChangeListener(event ->
                refreshState());

        contextMenuTargetListener = target.getElement()
                .addPropertyChangeListener("_contextMenuTargetTabId", __ -> refreshState());
    }

    @Override
    protected boolean hasCloseableTabs() {
        if (target.getTabsStream().count() <= 1) {
            return false;
        }

        Tab actionTab = findActionTab();
        return target.getTabsStream()
                .anyMatch(tab ->
                        !tab.equals(actionTab)
                                && tab instanceof ViewTab viewTab
                                && viewTab.isClosable()
                );
    }

    @Override
    public void execute(@Nullable Component trigger) {
        checkTarget();

        Tab savedTab = findTab(trigger);
        if (savedTab == null) {
            log.warn("Cannot close other tabs because cannot find trigger tab");
        }

        List<Views.ViewStack> viewStacks = target.getTabsStream()
                .filter(tab -> !tab.equals(savedTab))
                .map(tab -> target.getComponent(tab))
                .map(this::asViewStack)
                .toList();

        closeViewStacks(viewStacks);
    }
}

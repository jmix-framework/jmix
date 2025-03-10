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
import io.jmix.tabbedmode.component.tabsheet.JmixViewTab;
import io.jmix.tabbedmode.component.tabsheet.MainTabSheetUtils;
import io.jmix.tabbedmode.component.workarea.TabbedViewsContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;

@ActionType(CloseOthersTabsAction.ID)
public class CloseOthersTabsAction extends TabbedViewsContainerAction<CloseOthersTabsAction> {

    private static final Logger log = LoggerFactory.getLogger(CloseOthersTabsAction.class);

    public static final String ID = "tabmod_closeOthersTabs";

    protected Registration tabsCollectionChangeListener;
    protected Registration contextMenuTargetListener;

    public CloseOthersTabsAction() {
        this(ID);
    }

    public CloseOthersTabsAction(String id) {
        super(id);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.closeOthersTabs.text");
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
    protected boolean isApplicable() {
        return super.isApplicable() && hasCloseableTabs();
    }

    protected boolean hasCloseableTabs() {
        if (target.getTabs().size() <= 1) {
            return false;
        }

        Tab actionTab = findActionTab();
        return target.getTabs().stream()
                .anyMatch(tab ->
                        !tab.equals(actionTab)
                                && tab instanceof JmixViewTab viewTab
                                && viewTab.isClosable()
                );
    }

    @Override
    public void execute(Component component) {
        checkTarget();

        if (component instanceof JmixViewTab savedTab) {
            new HashSet<>(target.getTabs()).stream()
                    .filter(tab -> !tab.equals(savedTab) && tab instanceof JmixViewTab)
                    .forEach(tab -> MainTabSheetUtils.closeTab(((JmixViewTab) tab)));
        } else {
            log.warn("Cannot close other tabs because the component is not a '{}'",
                    JmixViewTab.class.getName());
        }
    }
}

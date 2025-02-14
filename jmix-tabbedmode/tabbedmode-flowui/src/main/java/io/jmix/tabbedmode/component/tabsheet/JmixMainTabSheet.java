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

package io.jmix.tabbedmode.component.tabsheet;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.tabs.Tab;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasActions;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.stream.Stream;

@JsModule("./src/tabsheet/mainTabSheetConnector.ts")
public class JmixMainTabSheet extends JmixTabSheet implements HasActions {

    private MainTabSheetActionsSupport actionsSupport;

    public void removeAll() {
        new ArrayList<>(tabToContent.keySet())
                .forEach(this::remove);
    }

    public Set<Tab> getTabs() {
        return Collections.unmodifiableSet(tabToContent.keySet());
    }

    // TODO: gg, add collection version
    public Stream<Component> getTabComponentsStream() {
        return tabToContent.values().stream();
    }

    // TODO: gg, issue to add selected
    @Nullable
    @Override
    public Tab getSelectedTab() {
        return super.getSelectedTab();
    }

    @Override
    public void addAction(Action action) {
        getActionsSupport().addAction(action);
    }

    @Override
    public void addAction(Action action, int index) {
        getActionsSupport().addAction(action, index);
    }

    @Override
    public void removeAction(Action action) {
        getActionsSupport().removeAction(action);
    }

    @Override
    public Collection<Action> getActions() {
        return getActionsSupport().getActions();
    }

    @Nullable
    @Override
    public Action getAction(String id) {
        return getActionsSupport().getAction(id).orElse(null);
    }

    public Tab getTab(String id) {
        return findTab(id).orElseThrow(() ->
                new IllegalArgumentException("Not found tab with id: '%s'".formatted(id)));
    }

    public Optional<Tab> findTab(String id) {
        return getTabs().stream()
                .filter(tab -> UiComponentUtils.sameId(tab, id))
                .findAny();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        initConnector();
    }

    protected MainTabSheetActionsSupport getActionsSupport() {
        if (actionsSupport == null) {
            actionsSupport = createActionsSupport();
        }

        return actionsSupport;
    }

    protected MainTabSheetActionsSupport createActionsSupport() {
        return new MainTabSheetActionsSupport(this);
    }

    protected void initConnector() {
        getElement().executeJs(
                "window.Vaadin.Flow.mainTabSheetConnector.initLazy(this)");
    }

    @ClientCallable
    private void updateContextMenuTargetTab(String tabId) {
        getElement().setProperty("_contextMenuTargetTabId", tabId);
    }
}

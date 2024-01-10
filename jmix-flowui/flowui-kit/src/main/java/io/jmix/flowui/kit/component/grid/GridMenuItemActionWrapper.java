/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.kit.component.grid;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasSuffix;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;

import jakarta.annotation.Nullable;
import java.util.Optional;

public class GridMenuItemActionWrapper<T> implements HasText, HasComponents, HasEnabled, HasPrefix, HasSuffix,
        HasTooltip {

    protected final GridMenuItem<T> menuItem;
    protected GridContextMenuItemComponent component;

    protected GridMenuItemActionSupport actionSupport;

    public GridMenuItemActionWrapper(GridMenuItem<T> menuItem, GridContextMenuItemComponent component) {
        this.menuItem = menuItem;
        this.component = component;
    }

    public GridMenuItem<T> getMenuItem() {
        return menuItem;
    }

    @Nullable
    public Action getAction() {
        return getActionSupport().getAction();
    }

    public void setAction(@Nullable Action action) {
        getActionSupport().setAction(action);
    }

    public GridContextMenuItemComponent getComponent() {
        return component;
    }

    public Registration addMenuItemClickListener(
            ComponentEventListener<GridContextMenu.GridContextMenuItemClickEvent<T>> clickListener) {
        return menuItem.addMenuItemClickListener(clickListener);
    }

    public boolean isCheckable() {
        return menuItem.isCheckable();
    }

    public void setCheckable(boolean checkable) {
        menuItem.setCheckable(checkable);
    }

    public boolean isChecked() {
        return menuItem.isChecked();
    }

    public void setChecked(boolean checked) {
        menuItem.setChecked(checked);
    }

    public Optional<String> getId() {
        return menuItem.getId();
    }

    public void setId(String id) {
        menuItem.setId(id);
    }

    public boolean isVisible() {
        return menuItem.isVisible();
    }

    public void setVisible(boolean visible) {
        menuItem.setVisible(visible);
    }

    @Override
    public boolean isEnabled() {
        return menuItem.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        menuItem.setEnabled(enabled);
    }

    @Override
    public String getText() {
        return component.getText();
    }

    @Override
    public void setText(String text) {
        component.setText(text);
    }

    @Override
    public Component getPrefixComponent() {
        return component.getPrefixComponent();
    }

    @Override
    public void setPrefixComponent(Component prefixComponent) {
        component.setPrefixComponent(prefixComponent);
    }

    @Override
    public Component getSuffixComponent() {
        return component.getSuffixComponent();
    }

    @Override
    public void setSuffixComponent(Component suffixComponent) {
        component.setSuffixComponent(suffixComponent);
    }

    @Override
    public Tooltip getTooltip() {
        return component.getTooltip();
    }

    @Override
    public Tooltip setTooltipText(String text) {
        return component.setTooltipText(text);
    }

    @Override
    public WhiteSpace getWhiteSpace() {
        return component.getWhiteSpace();
    }

    @Override
    public void setWhiteSpace(WhiteSpace value) {
        component.setWhiteSpace(value);
    }

    @Override
    public void add(Component... components) {
        menuItem.add(components);
    }

    @Override
    public void add(String text) {
        menuItem.add(text);
    }

    @Override
    public void remove(Component... components) {
        menuItem.remove(components);
    }

    @Override
    public void removeAll() {
        menuItem.removeAll();
    }

    @Override
    public void addComponentAtIndex(int index, Component component) {
        menuItem.addComponentAtIndex(index, component);
    }

    @Override
    public void addComponentAsFirst(Component component) {
        menuItem.addComponentAsFirst(component);
    }

    @Override
    public Element getElement() {
        return menuItem.getElement();
    }

    public GridMenuItemActionSupport getActionSupport() {
        if (actionSupport == null) {
            actionSupport = createActionSupport();
        }

        return actionSupport;
    }

    protected GridMenuItemActionSupport createActionSupport() {
        return new GridMenuItemActionSupport(this);
    }

    public boolean isEmpty() {
        return component.isEmpty();
    }
}

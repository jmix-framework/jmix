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

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasSuffix;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasAction;
import jakarta.annotation.Nullable;

public class GridMenuItemActionWrapper<T> extends Composite<Div>
        implements HasText, HasComponents, HasPrefix, HasSuffix, HasTooltip, HasAction {

    protected static final String ITEM_COMPONENT_CLASS_NAME = "jmix-grid-context-menu-item-component";
    protected static final String PREFIX_COMPONENT_CLASS_NAME = "prefix-component";
    protected static final String TEXT_COMPONENT_CLASS_NAME = "text-component";
    protected static final String SUFFIX_COMPONENT_CLASS_NAME = "suffix-component";

    protected Span textComponent;
    protected Component prefixComponent;
    protected Component suffixComponent;
    protected Tooltip tooltip;

    protected GridMenuItem<T> menuItem;

    protected GridMenuItemActionSupport actionSupport;

    public GridMenuItemActionWrapper() {
    }

    public GridMenuItemActionWrapper(GridMenuItem<T> menuItem) {
        this.menuItem = menuItem;
    }

    @Override
    protected Div initContent() {
        Div div = super.initContent();
        div.setClassName(ITEM_COMPONENT_CLASS_NAME);

        return div;
    }

    public GridMenuItem<T> getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(GridMenuItem<T> menuItem) {
        this.menuItem = menuItem;
    }

    @Nullable
    @Override
    public Action getAction() {
        return getActionSupport().getAction();
    }

    @Override
    public void setAction(@Nullable Action action, boolean overrideComponentProperties) {
        getActionSupport().setAction(action, overrideComponentProperties);
    }

    public Registration addMenuItemClickListener(
            ComponentEventListener<GridContextMenu.GridContextMenuItemClickEvent<T>> clickListener) {
        return menuItem.addMenuItemClickListener(clickListener);
    }

    public boolean isCheckable() {
        return menuItem != null && menuItem.isCheckable();
    }

    public void setCheckable(boolean checkable) {
        if (menuItem != null) {
            menuItem.setCheckable(checkable);
        }
    }

    public boolean isChecked() {
        return menuItem != null && menuItem.isChecked();
    }

    public void setChecked(boolean checked) {
        if (menuItem != null) {
            menuItem.setChecked(checked);
        }
    }

    @Override
    @Nullable
    public String getText() {
        return textComponent != null ? textComponent.getText() : null;
    }

    @Override
    public void setText(@Nullable String text) {
        if (Strings.isNullOrEmpty(text)) {
            updateContent(prefixComponent, null, suffixComponent);
        } else {
            if (textComponent == null) {
                textComponent = new Span(text);
            } else {
                textComponent.setText(text);
            }
            updateContent(prefixComponent, textComponent, suffixComponent);
        }
    }

    protected void updateContent(@Nullable Component prefixComponent,
                                 @Nullable Span textComponent,
                                 @Nullable Component suffixComponent) {
        setPrefixComponentInternal(prefixComponent);
        setTextComponentInternal(textComponent);
        setSuffixComponentInternal(suffixComponent);
    }

    protected void setPrefixComponentInternal(@Nullable Component prefixComponent) {
        if (this.prefixComponent != null) {
            this.prefixComponent.removeClassName(PREFIX_COMPONENT_CLASS_NAME);
            getContent().remove(this.prefixComponent);
        }

        this.prefixComponent = prefixComponent;
        if (prefixComponent != null) {
            prefixComponent.addClassName(PREFIX_COMPONENT_CLASS_NAME);
            getContent().addComponentAsFirst(prefixComponent);
        }
    }

    protected void setTextComponentInternal(@Nullable Span textComponent) {
        if (this.textComponent != null) {
            this.textComponent.removeClassName(TEXT_COMPONENT_CLASS_NAME);
            getContent().remove(this.textComponent);
        }

        this.textComponent = textComponent;
        if (textComponent != null) {
            this.textComponent.addClassName(TEXT_COMPONENT_CLASS_NAME);
            getContent().add(textComponent);
        }
    }

    protected void setSuffixComponentInternal(@Nullable Component suffixComponent) {
        if (this.suffixComponent != null) {
            this.suffixComponent.removeClassName(SUFFIX_COMPONENT_CLASS_NAME);
            getContent().remove(this.suffixComponent);
        }

        this.suffixComponent = suffixComponent;
        if (suffixComponent != null) {
            suffixComponent.addClassName(SUFFIX_COMPONENT_CLASS_NAME);
            getContent().add(suffixComponent);
        }
    }

    @Override
    @Nullable
    public Component getPrefixComponent() {
        return prefixComponent;
    }

    @Override
    public void setPrefixComponent(@Nullable Component component) {
        updateContent(component, textComponent, suffixComponent);
    }

    @Override
    @Nullable
    public Component getSuffixComponent() {
        return suffixComponent;
    }

    @Override
    public void setSuffixComponent(@Nullable Component component) {
        updateContent(prefixComponent, textComponent, component);
    }

    @Override
    public Tooltip getTooltip() {
        return getTooltipInternal();
    }

    protected Tooltip getTooltipInternal() {
        if (tooltip == null) {
            tooltip = Tooltip.forComponent(this);
        }
        return tooltip;
    }

    @Override
    public Tooltip setTooltipText(String text) {
        Tooltip tooltip = getTooltipInternal();

        tooltip.setText(text);
        return tooltip;
    }

    @Override
    public WhiteSpace getWhiteSpace() {
        return textComponent != null ? textComponent.getWhiteSpace() : WhiteSpace.NORMAL;
    }

    @Override
    public void setWhiteSpace(WhiteSpace value) {
        if (textComponent != null) {
            textComponent.setWhiteSpace(value);
        }
    }

    @Override
    public void add(Component... components) {
        getContent().add(components);
    }

    @Override
    public void add(String text) {
        getContent().add(text);
    }

    @Override
    public void remove(Component... components) {
        getContent().remove(components);
    }

    @Override
    public void removeAll() {
        getContent().removeAll();
    }

    @Override
    public void addComponentAtIndex(int index, Component component) {
        getContent().addComponentAtIndex(index, component);
    }

    @Override
    public void addComponentAsFirst(Component component) {
        getContent().addComponentAsFirst(component);
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
        return getContent().getChildren().findAny().isEmpty();
    }
}

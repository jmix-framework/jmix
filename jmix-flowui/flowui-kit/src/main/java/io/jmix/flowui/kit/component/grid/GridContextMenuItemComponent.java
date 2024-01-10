/*
 * Copyright 2023 Haulmont.
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
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasSuffix;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.component.shared.Tooltip;
import jakarta.annotation.Nullable;

/**
 * Represents some content with prefix, text and suffix for grid context menu item.
 *
 * @see GridMenuItemActionSupport
 * @see GridMenuItemActionWrapper
 */
public class GridContextMenuItemComponent extends Composite<Div> implements HasText, HasPrefix, HasSuffix, HasTooltip {

    protected static final String ITEM_COMPONENT_CLASS_NAME = "jmix-grid-context-menu-item-component";
    protected static final String PREFIX_COMPONENT_CLASS_NAME = "prefix-component";
    protected static final String TEXT_COMPONENT_CLASS_NAME = "text-component";
    protected static final String SUFFIX_COMPONENT_CLASS_NAME = "suffix-component";

    protected Span textComponent;
    protected Component prefixComponent;
    protected Component suffixComponent;
    protected Tooltip tooltip;

    @Override
    protected Div initContent() {
        Div div = super.initContent();
        div.setClassName(ITEM_COMPONENT_CLASS_NAME);

        return div;
    }

    @Override
    @Nullable
    public String getText() {
        return textComponent != null ? textComponent.getText() : null;
    }

    @Override
    public void setText(String text) {
        updateContent(prefixComponent, text, suffixComponent);
    }

    protected void updateContent(@Nullable Component prefixComponent,
                                 @Nullable String text,
                                 @Nullable Component suffixComponent) {
        setPrefixComponentInternal(prefixComponent);
        setTextComponentInternal(text);
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

    protected void setTextComponentInternal(@Nullable String title) {
        if (this.textComponent != null) {
            this.textComponent.removeClassName(TEXT_COMPONENT_CLASS_NAME);
            getContent().remove(this.textComponent);
        }

        if (Strings.isNullOrEmpty(title)) {
            this.textComponent = null;
        } else {
            this.textComponent = new Span(title);
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
    public void setPrefixComponent(@Nullable Component component) {
        updateContent(component, getText(), suffixComponent);
    }

    @Override
    @Nullable
    public Component getPrefixComponent() {
        return prefixComponent;
    }

    @Override
    public void setSuffixComponent(@Nullable Component component) {
        updateContent(prefixComponent, getText(), component);
    }

    @Override
    @Nullable
    public Component getSuffixComponent() {
        return suffixComponent;
    }

    @Override
    public Tooltip setTooltipText(String text) {
        Tooltip tooltip = getTooltipInternal();

        tooltip.setText(text);
        return tooltip;
    }

    protected Tooltip getTooltipInternal() {
        if (tooltip == null) {
            tooltip = Tooltip.forComponent(this);
        }
        return tooltip;
    }

    @Override
    public Tooltip getTooltip() {
        return getTooltipInternal();
    }

    public boolean isEmpty() {
        return textComponent == null && prefixComponent == null && suffixComponent == null;
    }
}

/*
 * Copyright 2019 Haulmont.
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

import com.haulmont.cuba.gui.components.TokenList;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomField;
import io.jmix.core.Entity;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.entity.EntityValues;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.widget.JmixScrollBoxLayout;
import io.jmix.ui.widget.JmixTokenListLabel;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nullable;
import java.util.*;

public class CubaTokenList<T extends Entity> extends CustomField<Collection<T>> {

    protected static final String TOKENLIST_STYLENAME = "jmix-tokenlist";
    protected static final String TOKENLIST_EDITOR_STYLENAME = "jmix-tokenlist-editor";
    protected static final String TOKENLIST_COMPOSITION_STYLENAME = "jmix-tokenlist-composition";
    protected static final String TOKENLIST_SCROLLBOX_STYLENAME = "jmix-tokenlist-scrollbox";
    protected static final String TOKENLIST_WRAPPER_STYLENAME = "jmix-tokenlist-wrapper";

    protected static final String ADD_BTN_STYLENAME = "add-btn";
    protected static final String CLEAR_BTN_STYLENAME = "clear-btn";
    protected static final String INLINE_STYLENAME = "inline";
    protected static final String READONLY_STYLENAME = "readonly";

    protected static final String POSITION_TOP_STYLENAME = "position-top";
    protected static final String POSITION_BOTTOM_STYLENAME = "position-bottom";

    @SuppressWarnings("UnusedAssignment")
    protected WebTokenList<T> owner = null;

    @SuppressWarnings("UnusedAssignment")
    protected CssLayout composition = null;
    protected CssLayout tokenContainerWrapper;
    @SuppressWarnings("UnusedAssignment")
    protected JmixScrollBoxLayout tokenContainer = null;
    protected CssLayout editor;

    protected Map<T, JmixTokenListLabel> itemComponents = new HashMap<>();
    protected Map<JmixTokenListLabel, T> componentItems = new HashMap<>();

    protected Subscription addButtonSub;
    protected Subscription clearBtnClickSubscription;

    public CubaTokenList(WebTokenList<T> owner) {
        this.owner = owner;

        composition = new CssLayout();
        composition.setWidthUndefined();
        composition.setStyleName(TOKENLIST_COMPOSITION_STYLENAME);

        tokenContainerWrapper = new CssLayout();
        tokenContainerWrapper.setStyleName(TOKENLIST_WRAPPER_STYLENAME);

        tokenContainer = new JmixScrollBoxLayout();
        tokenContainer.setStyleName(TOKENLIST_SCROLLBOX_STYLENAME);
        tokenContainer.setWidthUndefined();
        tokenContainer.setMargin(new MarginInfo(true, false, false, false));

        tokenContainerWrapper.addComponent(tokenContainer);
        composition.addComponent(tokenContainerWrapper);
        setPrimaryStyleName(TOKENLIST_STYLENAME);

        // do not trigger overridden method
        super.setWidth(-1, Unit.PIXELS);
    }

    @Override
    protected void doSetValue(Collection<T> value) {
        refreshTokens(value);
    }

    @Override
    public Collection<T> getValue() {
        return itemComponents.keySet();
    }

    @Override
    public boolean isEmpty() {
        return owner.getValueSource() != null
                ? CollectionUtils.isEmpty(owner.getValueSource().getValue())
                : CollectionUtils.isEmpty(getValue());
    }

    @Override
    public void setHeight(float height, Unit unit) {
        super.setHeight(height, unit);

        if (height > 0) {
            composition.setHeight("100%");
            tokenContainerWrapper.setHeight("100%");
        } else {
            composition.setHeightUndefined();
            tokenContainerWrapper.setHeightUndefined();
        }
    }

    @Override
    public void setWidth(float width, Unit unit) {
        super.setWidth(width, unit);

        // workaround for custom field call from constructor
        if (owner != null && composition != null && tokenContainer != null) {
            if (width > 0) {
                composition.setWidth("100%");
                editor.setWidth("100%");

                if (!owner.isSimple()) {
                    owner.lookupPickerField.setWidthFull();
                }
            } else {
                composition.setWidthUndefined();
                editor.setWidthUndefined();

                if (!owner.isSimple()) {
                    owner.lookupPickerField.setWidthAuto();
                }
            }
        }
    }

    @Override
    protected Component initContent() {
        return composition;
    }

    protected void initField() {
        if (editor == null) {
            editor = new CssLayout();
            editor.setWidthUndefined();
            editor.setStyleName(TOKENLIST_EDITOR_STYLENAME);
        }
        editor.removeAllComponents();

        if (!owner.isSimple()) {
            owner.lookupPickerField.setWidthAuto();
            editor.addComponent(ComponentsHelper.getComposition(owner.lookupPickerField));
        }
        owner.lookupPickerField.setVisible(!owner.isSimple());

        owner.addButton.setVisible(owner.isSimple());
        owner.addButton.setStyleName(ADD_BTN_STYLENAME);

        if (addButtonSub != null) {
            addButtonSub.remove();
        }

        if (!owner.isSimple()) {
            addButtonSub = owner.addButton.addClickListener(e -> {
                if (owner.isEditableWithParent()) {
                    owner.addValueFromLookupPickerField();
                }
                owner.addButton.focus();
            });
        } else {
            addButtonSub = owner.addButton.addClickListener(e ->
                    owner.openLookup(() -> owner.addButton.focus()));
        }
        Button vAddButton = owner.addButton.unwrapOrNull(Button.class);
        if (vAddButton != null) {
            editor.addComponent(vAddButton);
        }

        owner.clearButton.setVisible(owner.clearEnabled);
        owner.clearButton.setStyleName(CLEAR_BTN_STYLENAME);

        if (clearBtnClickSubscription != null) {
            clearBtnClickSubscription.remove();
        }
        clearBtnClickSubscription = owner.clearButton.addClickListener(e -> {
            clearValue();
            owner.clearButton.focus();
        });

        com.vaadin.ui.Button vClearButton = owner.clearButton.unwrapOrNull(com.vaadin.ui.Button.class);
        editor.addComponent(vClearButton);
    }

    protected void clearValue() {
        Collection<T> oldValue = new HashSet<>(getValue());

        for (JmixTokenListLabel label : new ArrayList<>(itemComponents.values())) {
            T item = componentItems.get(label);
            if (item != null) {
                itemComponents.remove(item);
                componentItems.remove(label);
            }

            if (owner.itemChangeHandler != null) {
                owner.itemChangeHandler.removeItem(item);
            }
        }

        if (owner.itemChangeHandler == null) {
            fireValueChangeEvent(oldValue);
        }
    }

    public void refreshComponent() {
        if (owner.inline) {
            addStyleName(INLINE_STYLENAME);
        } else {
            removeStyleName(INLINE_STYLENAME);
        }

        if (owner.isEditable()) {
            removeStyleName(READONLY_STYLENAME);
        } else {
            addStyleName(READONLY_STYLENAME);
        }

        if (editor != null) {
            composition.removeComponent(editor);
        }

        initField();

        if (owner.isEditableWithParent()) {
            if (owner.position == TokenList.Position.TOP) {
                composition.addComponentAsFirst(editor);
            } else {
                composition.addComponent(editor);
            }
        }

        updateTokenContainerVisibility();
        updateTokensEditable();
        updateEditorMargins();
        updateSizes();
    }

    protected void updateTokensEditable() {
        if (tokenContainer == null || tokenContainer.getComponentCount() == 0) {
            return;
        }

        boolean editable = owner.isEditableWithParent();

        for (int i = 0; i < tokenContainer.getComponentCount(); i++) {
            JmixTokenListLabel label = (JmixTokenListLabel) tokenContainer.getComponent(i);
            label.setEditable(editable);
        }
    }

    public void refreshTokens(@Nullable Collection<T> newValue) {
        tokenContainer.removeAllComponents();

        if (newValue == null) {
            newValue = Collections.emptyList();
        }

        List<T> usedItems = new ArrayList<>();

        for (T entity : newValue) {
            JmixTokenListLabel label = itemComponents.get(entity);

            if (label == null) {
                label = createToken();
                itemComponents.put(entity, label);
                componentItems.put(label, entity);
            }

            label.setEditable(owner.isEditableWithParent());
            label.setText(owner.getInstanceCaption(entity));
            label.setWidthUndefined();

            setTokenStyle(label, EntityValues.getId(entity));

            tokenContainer.addComponent(label);

            usedItems.add(entity);
        }

        for (T componentItem : new ArrayList<>(itemComponents.keySet())) {
            if (!usedItems.contains(componentItem)) {
                componentItems.remove(itemComponents.get(componentItem));
                itemComponents.remove(componentItem);
            }
        }

        updateTokenContainerVisibility();
        updateEditorMargins();
    }

    protected void updateTokenContainerVisibility() {
        tokenContainer.setVisible(!isEmpty());
    }

    protected void updateEditorMargins() {
        if (tokenContainer.isVisible()) {
            if (owner.position == TokenList.Position.TOP) {
                editor.removeStyleName(POSITION_BOTTOM_STYLENAME);
                editor.addStyleName(POSITION_TOP_STYLENAME);
            } else {
                editor.removeStyleName(POSITION_TOP_STYLENAME);
                editor.addStyleName(POSITION_BOTTOM_STYLENAME);
            }
        } else {
            editor.removeStyleNames(POSITION_TOP_STYLENAME, POSITION_BOTTOM_STYLENAME);
        }
    }

    protected void updateSizes() {
        if (getHeight() > 0) {
            composition.setHeight("100%");
            tokenContainerWrapper.setHeight("100%");
        } else {
            composition.setHeightUndefined();
            tokenContainerWrapper.setHeightUndefined();
        }

        if (getWidth() > 0) {
            composition.setWidth("100%");
            editor.setWidth("100%");

            if (!owner.isSimple()) {
                owner.lookupPickerField.setWidthFull();
            }
        } else {
            composition.setWidthUndefined();
            editor.setWidthUndefined();

            if (!owner.isSimple()) {
                owner.lookupPickerField.setWidthAuto();
            }
        }
    }

    public void refreshClickListeners(@Nullable TokenList.ItemClickListener listener) {
        Collection<T> value;

        if (owner.getValueSource() != null) {
            value = owner.getValueSourceValue();
        } else {
            value = getValue();
        }

        for (T entity : value) {
            JmixTokenListLabel label = itemComponents.get(entity);
            if (label != null) {
                if (listener != null) {
                    label.setClickListener(source ->
                            doClick(label));
                } else {
                    label.setClickListener(null);
                }
            }
        }
    }

    protected JmixTokenListLabel createToken() {
        JmixTokenListLabel label = new JmixTokenListLabel();
        label.setWidth("100%");
        label.addListener((JmixTokenListLabel.RemoveTokenListener) source -> {
            if (owner.isEditableWithParent()) {
                doRemove(source);
            }
        });
        return label;
    }

    protected void doRemove(JmixTokenListLabel source) {
        T item = componentItems.get(source);
        if (item != null) {
            Collection<T> oldValue = new HashSet<>(getValue());

            itemComponents.remove(item);
            componentItems.remove(source);

            if (owner.itemChangeHandler != null) {
                owner.itemChangeHandler.removeItem(item);
            } else {
                fireValueChangeEvent(oldValue);
            }
        }
    }

    protected void doClick(JmixTokenListLabel source) {
        if (owner.itemClickListener != null) {
            Object item = componentItems.get(source);
            if (item != null) {
                owner.itemClickListener.onClick(item);
            }
        }
    }

    protected void setTokenStyle(JmixTokenListLabel label, @Nullable Object itemId) {
        if (owner.getTokenStyleGenerator() != null) {
            String styleName = owner.getTokenStyleGenerator().apply(itemId);
            if (styleName != null && !styleName.isEmpty()) {
                label.setStyleName(styleName);
            }
        }
    }

    protected void fireValueChangeEvent(Collection<T> oldValue) {
        ValueChangeEvent<Collection<T>> event = new ValueChangeEvent<>(this, oldValue, true);
        fireEvent(event);
    }
}

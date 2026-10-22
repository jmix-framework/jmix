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

package io.jmix.flowui.kit.component.valuepicker;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.delegate.AbstractActionsHolderSupport;

import java.util.HashMap;
import java.util.Map;

/**
 * A support class responsible for managing actions in a value picker component.
 * This class provides the functionality to add, remove, and manage action buttons for the component
 * while updating the actions layout dynamically.
 */
public class ValuePickerActionSupport extends AbstractActionsHolderSupport<Component> {

    protected static final String SLOT_ACTIONS = "actions";
    protected static final String ATTRIBUTE_HAS_ACTIONS = "has-actions";
    protected static final String ATTRIBUTE_HAS_READONLY_ACTIONS = "has-readonly-actions";

    protected final String actionsSlot;
    protected final String hasActionsAttribute;

    protected Div actionsLayout;

    protected Map<Action, ValuePickerButton> actionBinding = new HashMap<>();
    protected Map<Action, Registration> actionVisibilityRegistrations = new HashMap<>();

    public ValuePickerActionSupport(Component component) {
        this(component, SLOT_ACTIONS, ATTRIBUTE_HAS_ACTIONS);
    }

    public ValuePickerActionSupport(Component component,
                                    String actionsSlot,
                                    String hasActionsAttribute) {
        super(component);
        this.actionsSlot = actionsSlot;
        this.hasActionsAttribute = hasActionsAttribute;
    }

    @Override
    protected void addActionInternal(Action action, int index) {
        super.addActionInternal(action, index);

        addButton(action);
        updateActionsSlot();
    }

    @Override
    protected boolean removeActionInternal(Action action) {
        if (super.removeActionInternal(action)) {
            removeButton(action);
            updateActionsSlot();

            return true;
        }

        return false;
    }

    protected void addButton(Action action) {
        int index = actions.indexOf(action);
        ValuePickerButton button = createButton();
        button.setAction(action);
        getActionsLayout().addComponentAtIndex(index, button);

        actionBinding.put(action, button);

        updateButtonVisibility(action);
        // Registered after the button's own listener, so the read-only rule has the last word.
        actionVisibilityRegistrations.put(action, action.addPropertyChangeListener(event -> {
            if (Action.PROP_VISIBLE.equals(event.getPropertyName())) {
                updateButtonVisibility(action);
            }
        }));
    }

    protected void removeButton(Action action) {
        Registration registration = actionVisibilityRegistrations.remove(action);
        if (registration != null) {
            registration.remove();
        }

        ValuePickerButton button = actionBinding.remove(action);
        button.setAction(null);
        getActionsLayout().remove(button);
    }

    protected void updateActionsSlot() {
        if (getActions().isEmpty()) {
            SlotUtils.clearSlot(component, actionsSlot);
            getElement().removeAttribute(hasActionsAttribute);
        } else {
            SlotUtils.addToSlot(component, actionsSlot, getActionsLayout());
            getElement().setAttribute(hasActionsAttribute, true);
        }

        updateHasReadOnlyActionsAttribute();
    }

    protected void updateHasReadOnlyActionsAttribute() {
        if (getActions().stream().anyMatch(this::isActionVisibleWhenReadOnly)) {
            getElement().setAttribute(ATTRIBUTE_HAS_READONLY_ACTIONS, true);
        } else {
            getElement().removeAttribute(ATTRIBUTE_HAS_READONLY_ACTIONS);
        }
    }

    /**
     * Updates the visibility of action buttons according to the read-only state of the component.
     * Must be called when the read-only state of the component changes.
     */
    public void updateReadOnlyState() {
        for (Action action : actions) {
            updateButtonVisibility(action);
        }

        updateHasReadOnlyActionsAttribute();
    }

    protected void updateButtonVisibility(Action action) {
        ValuePickerButton button = actionBinding.get(action);
        if (button != null) {
            button.setVisible(action.isVisible()
                    && (!isReadOnly() || isActionVisibleWhenReadOnly(action)));
        }
    }

    @Override
    protected void performActionByShortcut(Action action) {
        if (!isReadOnly() || isActionVisibleWhenReadOnly(action)) {
            super.performActionByShortcut(action);
        }
    }

    protected boolean isReadOnly() {
        return component instanceof HasValue<?, ?> hasValue && hasValue.isReadOnly();
    }

    protected boolean isActionVisibleWhenReadOnly(Action action) {
        return false;
    }

    protected Div getActionsLayout() {
        if (actionsLayout == null) {
            actionsLayout = createActionsLayout();
        }

        return actionsLayout;
    }

    protected ValuePickerButton createButton() {
        return new ValuePickerButton();
    }

    protected Div createActionsLayout() {
        return new Div();
    }

    protected Element getElement() {
        return component.getElement();
    }
}

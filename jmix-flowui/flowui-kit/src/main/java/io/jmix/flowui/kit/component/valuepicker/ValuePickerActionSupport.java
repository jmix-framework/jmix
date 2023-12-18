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
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.dom.Element;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.delegate.AbstractActionsHolderSupport;

import java.util.HashMap;
import java.util.Map;

public class ValuePickerActionSupport extends AbstractActionsHolderSupport<Component> {

    protected static final String SLOT_ACTIONS = "actions";
    protected static final String ATTRIBUTE_HAS_ACTIONS = "has-actions";

    protected final String actionsSlot;
    protected final String hasActionsAttribute;

    protected Div actionsLayout;

    protected Map<Action, ValuePickerButton> actionBinding = new HashMap<>();

    /**
     * @deprecated use one of {@link ValuePickerActionSupport#ValuePickerActionSupport(Component)},
     * {@link ValuePickerActionSupport#ValuePickerActionSupport(Component, String, String)}
     */
    @Deprecated(since = "3.0", forRemoval = true)
    public ValuePickerActionSupport(HasElement component) {
        this(component, SLOT_ACTIONS, ATTRIBUTE_HAS_ACTIONS);
    }

    /**
     * @deprecated use one of {@link ValuePickerActionSupport#ValuePickerActionSupport(Component)},
     * {@link ValuePickerActionSupport#ValuePickerActionSupport(Component, String, String)}
     */
    @Deprecated(since = "3.0", forRemoval = true)
    public ValuePickerActionSupport(HasElement component,
                                    String actionsSlot,
                                    String hasActionsAttribute) {
        super((Component) component);
        this.actionsSlot = actionsSlot;
        this.hasActionsAttribute = hasActionsAttribute;
    }

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

        addButton(action, index);
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

    protected void addButton(Action action, int index) {
        ValuePickerButton button = new ValuePickerButton();
        button.setAction(action);
        getActionsLayout().addComponentAtIndex(index, button);

        actionBinding.put(action, button);
    }

    protected void removeButton(Action action) {
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
    }

    protected Div getActionsLayout() {
        if (actionsLayout == null) {
            actionsLayout = createActionsLayout();
        }

        return actionsLayout;
    }

    protected Div createActionsLayout() {
        return new Div();
    }

    protected Element getElement() {
        return component.getElement();
    }
}

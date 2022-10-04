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

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;
import io.jmix.flowui.kit.action.Action;

import java.util.*;

import static io.jmix.flowui.kit.component.FlowuiComponentUtils.*;

public class ValuePickerActionSupport {

    protected static final String SLOT_ACTIONS = "actions";
    protected static final String ATTRIBUTE_HAS_ACTIONS = "has-actions";

    protected final HasElement component;
    protected final String actionsSlot;
    protected final String hasActionsAttribute;

    protected Div actionsLayout;

    protected List<Action> actions = new ArrayList<>();
    protected Map<Action, ValuePickerButton> actionBinding = new HashMap<>();

    public ValuePickerActionSupport(HasElement component) {
        this(component, SLOT_ACTIONS, ATTRIBUTE_HAS_ACTIONS);
    }

    public ValuePickerActionSupport(HasElement component,
                                    String actionsSlot,
                                    String hasActionsAttribute) {
        this.component = component;
        this.actionsSlot = actionsSlot;
        this.hasActionsAttribute = hasActionsAttribute;
    }

    public void addAction(Action action) {
        addAction(action, actions.size());
    }

    public void addAction(Action action, int index) {
        Preconditions.checkNotNull(action, "Action cannot be null");

        addActionInternal(action, index);
    }

    protected void addActionInternal(Action action, int index) {
        int oldIndex = findActionIndexById(actions, action.getId());
        if (oldIndex >= 0) {
            removeActionInternal(actions.get(oldIndex));
            if (index > oldIndex) {
                index--;
            }
        }

        actions.add(index, action);
        addButton(action, index);

        updateActionsSlot();
    }

    public void removeAction(Action action) {
        Preconditions.checkNotNull(action, "Action cannot be null");

        removeActionInternal(action);
    }

    protected void removeActionInternal(Action action) {
        if (actions.remove(action)) {
            removeButton(action);
            updateActionsSlot();
        }
    }

    public Optional<Action> getAction(String id) {
        return getActions().stream()
                .filter(action ->
                        Objects.equals(action.getId(), id))
                .findFirst();
    }

    public Collection<Action> getActions() {
        return Collections.unmodifiableList(actions);
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
            clearSlot(getElement(), actionsSlot);
            getElement().removeAttribute(hasActionsAttribute);
        } else {
            addComponentsToSlot(getElement(), actionsSlot, getActionsLayout());
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

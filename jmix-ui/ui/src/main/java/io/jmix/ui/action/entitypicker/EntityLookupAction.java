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

package io.jmix.ui.action.entitypicker;

import io.jmix.core.DevelopmentException;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.action.AbstractLookupAction;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.builder.LookupBuilder;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

/**
 * Standard action for setting an entity to the entity picker using the entity lookup screen.
 * <p>
 * Should be defined for {@link EntityPicker} or its subclass in a screen XML descriptor.
 * <p>
 * The action instance can be parameterized using the nested {@code properties} XML element or programmatically in the
 * screen controller.
 *
 * @param <E> type of entity
 */
@StudioAction(
        target = "io.jmix.ui.component.EntityPicker",
        description = "Sets an entity to the entity picker using the entity lookup screen")
@ActionType(EntityLookupAction.ID)
public class EntityLookupAction<E> extends AbstractLookupAction<E>
        implements EntityPicker.EntityPickerAction, Action.ScreenOpeningAction, InitializingBean,
        Action.ExecutableAction {

    public static final String ID = "entity_lookup";

    protected EntityPicker entityPicker;

    protected UiComponentProperties componentProperties;

    protected boolean editable = true;

    public EntityLookupAction() {
        super(EntityLookupAction.ID);
    }

    public EntityLookupAction(String id) {
        super(id);
    }

    @Override
    public void setEntityPicker(@Nullable EntityPicker entityPicker) {
        this.entityPicker = entityPicker;
    }

    @Override
    public void editableChanged(boolean editable) {
        setEditable(editable);
        if (editable) {
            setIcon(icons.get(JmixIcon.ENTITYPICKER_LOOKUP));
        } else {
            setIcon(icons.get(JmixIcon.ENTITYPICKER_LOOKUP_READONLY));
        }
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

    protected void setEditable(boolean editable) {
        boolean oldValue = this.editable;
        if (oldValue != editable) {
            this.editable = editable;
            firePropertyChange(PROP_EDITABLE, oldValue, editable);
        }
    }

    @Autowired
    protected void setUiComponentProperties(UiComponentProperties componentProperties) {
        this.componentProperties = componentProperties;
    }

    @Override
    public void afterPropertiesSet() {
        setIcon(icons.get(JmixIcon.ENTITYPICKER_LOOKUP));
        setShortcut(componentProperties.getPickerLookupShortcut());

        if (getShortcutCombination() != null) {
            setDescription(messages.getMessage("entityPicker.action.lookup.tooltip")
                    + " (" + getShortcutCombination().format() + ")");
        } else {
            setDescription(messages.getMessage("entityPicker.action.lookup.tooltip"));
        }
    }

    @Override
    public void actionPerform(Component component) {
        // if standard behaviour
        if (!hasSubscriptions(ActionPerformedEvent.class)) {
            execute();
        } else {
            // call action perform handlers from super, delegate execution
            super.actionPerform(component);
        }
    }

    /**
     * Executes the action.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        MetaClass metaClass = entityPicker.getMetaClass();
        if (metaClass == null) {
            throw new DevelopmentException("Neither metaClass nor dataContainer/property is specified " +
                    "for the EntityPicker", "action ID", getId());
        }

        LookupBuilder builder = screenBuilders.lookup(entityPicker);

        builder = screenInitializer.initBuilder(builder);

        if (selectValidator != null) {
            builder = builder.withSelectValidator(selectValidator);
        }

        if (transformation != null) {
            builder = builder.withTransformation(transformation);
        }

        Screen lookupScreen = builder.build();

        screenInitializer.initScreen(lookupScreen);

        lookupScreen.show();
    }
}

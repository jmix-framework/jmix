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

import io.jmix.core.Messages;
import io.jmix.core.Entity;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.UiProperties;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.UiControllerUtils;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Standard entity picker action for clearing the field value.
 * <p>
 * Should be defined for {@link EntityPicker} or its subclass in a screen XML descriptor.
 */
@StudioAction(category = "EntityPicker Actions", description = "Clears the entity picker value")
@ActionType(ClearAction.ID)
public class ClearAction extends BaseAction implements EntityPicker.EntityPickerAction, InitializingBean {

    public static final String ID = "entity_clear";

    protected EntityPicker entityPicker;
    protected Icons icons;
    protected Messages messages;
    protected UiProperties properties;

    protected boolean editable = true;

    public ClearAction() {
        super(ID);
    }

    public ClearAction(String id) {
        super(id);
    }

    @Autowired
    protected void setUiProperties(UiProperties properties) {
        this.properties = properties;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Override
    public void afterPropertiesSet() {
        setShortcut(properties.getPickerClearShortcut());
        setDescription(messages.getMessage("entityPicker.action.clear.tooltip")
                + " (" + getShortcutCombination().format() + ")");
    }

    @Override
    public void setEntityPicker(@Nullable EntityPicker entityPicker) {
        this.entityPicker = entityPicker;
    }

    @Override
    public void editableChanged(boolean editable) {
        setEditable(editable);

        if (editable) {
            setIcon(icons.get(JmixIcon.ENTITYPICKER_CLEAR));
        } else {
            setIcon(icons.get(JmixIcon.ENTITYPICKER_CLEAR_READONLY));
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
    protected void setIcons(Icons icons) {
        this.icons = icons;

        setIcon(icons.get(JmixIcon.ENTITYPICKER_CLEAR));
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
    public void execute() {
        // remove entity if it is a composition
        Object value = entityPicker.getValue();
        ValueSource valueSource = entityPicker.getValueSource();
        if (value != null && !value.equals(entityPicker.getEmptyValue()) && valueSource instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) entityPicker.getValueSource();
            Entity entity = (Entity) entityPicker.getValue();
            if (entityValueSource.getMetaPropertyPath() != null
                    && entityValueSource.getMetaPropertyPath().getMetaProperty().getType() == MetaProperty.Type.COMPOSITION) {
                FrameOwner screen = entityPicker.getFrame().getFrameOwner();
                DataContext dataContext = UiControllerUtils.getScreenData(screen).getDataContext();
                dataContext.remove(entity);
            }
        }
        // Set the value as if the user had set it
        entityPicker.setValueFromUser(entityPicker.getEmptyValue());
    }
}

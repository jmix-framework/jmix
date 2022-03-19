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

import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.valuepicker.ValueClearAction;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.component.ValuePicker;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.UiControllerUtils;

import javax.annotation.Nullable;

/**
 * Standard entity picker action for clearing the field value.
 * <p>
 * Should be defined for {@link EntityPicker} or its subclass in a screen XML descriptor.
 */
@StudioAction(target = "io.jmix.ui.component.EntityPicker", description = "Clears the entity picker value")
@ActionType(EntityClearAction.ID)
public class EntityClearAction extends ValueClearAction implements EntityPicker.EntityPickerAction {

    public static final String ID = "entity_clear";

    public EntityClearAction() {
        this(ID);
    }

    public EntityClearAction(String id) {
        super(id);
    }

    @Override
    public void setEntityPicker(@Nullable EntityPicker entityPicker) {
        setPicker(entityPicker);
    }

    @Override
    public void setPicker(@Nullable ValuePicker valuePicker) {
        if (valuePicker != null && !(valuePicker instanceof EntityPicker)) {
            throw new IllegalArgumentException("Incorrect component type. Must be " +
                    "'EntityPicker' or its inheritors");
        }

        super.setPicker(valuePicker);
    }

    protected EntityPicker getEntityPicker() {
        return (EntityPicker) valuePicker;
    }

    /**
     * Executes the action.
     */
    @Override
    public void execute() {
        EntityPicker entityPicker = getEntityPicker();

        // remove entity if it is a composition
        Object value = entityPicker.getValue();
        ValueSource valueSource = entityPicker.getValueSource();
        if (value != null && !value.equals(entityPicker.getEmptyValue())
                && valueSource instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) entityPicker.getValueSource();
            Object entity = entityPicker.getValue();
            if (entityValueSource.getMetaPropertyPath() != null
                    && entityValueSource.getMetaPropertyPath().getMetaProperty().getType() == MetaProperty.Type.COMPOSITION) {
                FrameOwner screen = entityPicker.getFrame().getFrameOwner();
                DataContext dataContext = UiControllerUtils.getScreenData(screen).getDataContext();
                dataContext.remove(entity);
            }
        }

        super.execute();
    }
}

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

package io.jmix.ui.component.factory;

import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.Actions;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.entitypicker.EntityLookupAction;
import io.jmix.ui.action.entitypicker.EntityOpenAction;
import io.jmix.ui.action.entitypicker.EntityOpenCompositionAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.impl.EntityFieldCreationSupport;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.screen.MapScreenOptions;
import io.jmix.ui.screen.OpenMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
@org.springframework.stereotype.Component("ui_DataGridEditorComponentGenerationStrategy")
public class DataGridEditorComponentGenerationStrategy extends AbstractComponentGenerationStrategy implements Ordered {

    @Autowired
    public DataGridEditorComponentGenerationStrategy(Messages messages,
                                                     UiComponents uiComponents,
                                                     EntityFieldCreationSupport entityFieldCreationSupport,
                                                     Metadata metadata,
                                                     MetadataTools metadataTools,
                                                     Icons icons,
                                                     Actions actions) {
        super(messages, uiComponents, entityFieldCreationSupport, metadata, metadataTools, icons, actions);
    }

    @Nullable
    @Override
    public Component createComponent(ComponentGenerationContext context) {
        if (context.getTargetClass() == null
                || !DataGrid.class.isAssignableFrom(context.getTargetClass())) {
            return null;
        }

        return createComponentInternal(context);
    }

    @Override
    protected Component createStringField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        TextField component = uiComponents.create(TextField.class);
        setValueSource(component, context);
        return component;
    }

    private void initActionScreenParameters(@Nullable Action.ScreenOpeningAction action) {
        if (action != null) {
            // Opening screen in another mode will close editor
            action.setOpenMode(OpenMode.DIALOG);
            // In case of adding special logic for a screen opened from DataGrid editor
            action.setScreenOptionsSupplier(() ->
                    new MapScreenOptions(ParamsMap.of("dataGridEditor", true)));
        }
    }

    @Override
    protected Field createEntityField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        EntityPicker field = entityFieldCreationSupport.createEntityField(mpp, context.getOptions());
        setValueSource(field, context);

        initActionScreenParameters((EntityLookupAction) field.getAction(EntityLookupAction.ID));
        initActionScreenParameters((EntityOpenAction) field.getAction(EntityOpenAction.ID));
        initActionScreenParameters((EntityOpenCompositionAction) field.getAction(EntityOpenCompositionAction.ID));

        return field;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 30;
    }
}

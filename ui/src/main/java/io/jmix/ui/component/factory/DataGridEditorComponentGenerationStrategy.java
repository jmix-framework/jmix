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
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.entitypicker.LookupAction;
import io.jmix.ui.action.entitypicker.OpenAction;
import io.jmix.ui.action.entitypicker.OpenCompositionAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.impl.EntityFieldCreationSupport;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.screen.MapScreenOptions;
import io.jmix.ui.screen.OpenMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
@org.springframework.stereotype.Component(DataGridEditorComponentGenerationStrategy.NAME)
public class DataGridEditorComponentGenerationStrategy extends AbstractComponentGenerationStrategy implements Ordered {
    public static final String NAME = "ui_DataGridEditorComponentGenerationStrategy";

    @Autowired
    public DataGridEditorComponentGenerationStrategy(Messages messages,
                                                     UiComponents uiComponents,
                                                     EntityFieldCreationSupport entityFieldCreationSupport,
                                                     Metadata metadata,
                                                     MetadataTools metadataTools,
                                                     Icons icons) {
        super(messages, uiComponents, entityFieldCreationSupport, metadata, metadataTools, icons);
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
            action.setLaunchMode(OpenMode.DIALOG);
            // In case of adding special logic for a screen opened from DataGrid editor
            action.setScreenOptionsSupplier(() ->
                    new MapScreenOptions(ParamsMap.of("dataGridEditor", true)));
        }
    }

    @Override
    protected Field createEntityField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        EntityPicker field = entityFieldCreationSupport.createEntityField(mpp, context.getOptions());
        setValueSource(field, context);

        initActionScreenParameters((LookupAction) field.getAction(LookupAction.ID));
        initActionScreenParameters((OpenAction) field.getAction(OpenAction.ID));
        initActionScreenParameters((OpenCompositionAction) field.getAction(OpenCompositionAction.ID));

        return field;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 30;
    }
}

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

import io.jmix.core.FetchPlan;
import io.jmix.core.JmixEntity;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.entity.annotation.Lookup;
import io.jmix.core.entity.annotation.LookupType;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.Actions;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.entitypicker.EntityClearAction;
import io.jmix.ui.action.entitypicker.LookupAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentGenerationContext;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.EntityComboBox;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.component.Field;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.component.data.options.ContainerOptions;
import io.jmix.ui.component.impl.GuiActionSupport;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.DataComponents;
import io.jmix.ui.screen.MapScreenOptions;
import io.jmix.ui.screen.OpenMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

import javax.annotation.Nullable;

@org.springframework.stereotype.Component(DataGridEditorComponentGenerationStrategy.NAME)
public class DataGridEditorComponentGenerationStrategy extends AbstractComponentGenerationStrategy implements Ordered {
    public static final String NAME = "jmix_DataGridEditorMetaComponentStrategy";

    protected DataComponents dataComponents;

    @Autowired
    public void setDataComponents(DataComponents dataComponents) {
        this.dataComponents = dataComponents;
    }

    @Autowired
    public DataGridEditorComponentGenerationStrategy(Messages messages,
                                                     GuiActionSupport guiActionSupport,
                                                     Metadata metadata,
                                                     MetadataTools metadataTools) {
        super(messages, guiActionSupport, metadata, metadataTools);
    }

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Autowired
    public void setActions(Actions actions) {
        this.actions = actions;
    }

    @Nullable
    @Override
    public Component createComponent(ComponentGenerationContext context) {
        if (context.getComponentClass() == null
                || !DataGrid.class.isAssignableFrom(context.getComponentClass())) {
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

    @SuppressWarnings("unchecked")
    @Override
    protected Field createEntityField(ComponentGenerationContext context, MetaPropertyPath mpp) {
        Options options = context.getOptions();

        Lookup lookupAnnotation;
        if ((lookupAnnotation = mpp.getMetaProperty().getAnnotatedElement().getAnnotation(Lookup.class)) != null
                && lookupAnnotation.type() == LookupType.DROPDOWN) {
            MetaClass metaClass = mpp.getMetaProperty().getRange().asClass();
            CollectionContainer<JmixEntity> container = dataComponents.createCollectionContainer(metaClass.getJavaClass());
            CollectionLoader<JmixEntity> loader = dataComponents.createCollectionLoader();
            loader.setQuery("select e from " + metaClass.getName() + " e");
            loader.setFetchPlan(FetchPlan.INSTANCE_NAME);
            loader.setContainer(container);
            loader.load();
            options = new ContainerOptions(container);
        }

        EntityPicker entityPicker;
        if (options == null) {
            entityPicker = uiComponents.create(EntityPicker.class);
            setValueSource(entityPicker, context);
            LookupAction<?> lookupAction = (LookupAction<?>) actions.create(LookupAction.ID);
            // Opening lookup screen in another mode will close editor
            lookupAction.setOpenMode(OpenMode.DIALOG);
            // In case of adding special logic for lookup screen opened from DataGrid editor
            lookupAction.setScreenOptionsSupplier(() ->
                    new MapScreenOptions(ParamsMap.of("dataGridEditor", true)));
            entityPicker.addAction(lookupAction);
            boolean actionsByMetaAnnotations = guiActionSupport.createActionsByMetaAnnotations(entityPicker);
            if (!actionsByMetaAnnotations) {
                entityPicker.addAction(actions.create(EntityClearAction.ID));
            }
        } else {
            EntityComboBox entityComboBox = uiComponents.create(EntityComboBox.class);
            setValueSource(entityComboBox, context);
            entityComboBox.setOptions(options);

            entityPicker = entityComboBox;

            guiActionSupport.createActionsByMetaAnnotations(entityPicker);
        }

        return entityPicker;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PLATFORM_PRECEDENCE + 30;
    }
}

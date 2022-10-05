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

package io.jmix.datatoolsflowui.action;

import io.jmix.datatoolsflowui.view.entityinspector.EntityInspectorDetailView;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.list.EditAction;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.view.DetailView;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.builder.DetailWindowBuilder;

@ActionType(EntityInspectorEditAction.ID)
public class EntityInspectorEditAction<E> extends EditAction<E> {

    public static final String ID = "entity_inspector_edit";

    protected String entityMetaClass;
    protected String entityId;
    protected DataContext parentDataContext;
    protected String parentProperty;
    protected CollectionContainer<Object> parentDataContainer;

    public EntityInspectorEditAction() {
        this(ID);
    }

    public EntityInspectorEditAction(String id) {
        super(id);
    }

    @Override
    protected void openDialog(E editedEntity) {
        DetailWindowBuilder<E, View<?>> builder = dialogWindows.detail(target);

        builder = viewInitializer.initWindowBuilder(builder);

        builder = builder.editEntity(editedEntity);

        if (parentDataContext != null) {
            builder = builder.withParentDataContext(parentDataContext);
        }

        if (transformation != null) {
            builder = builder.withTransformation(transformation);
        }

        DialogWindow<View<?>> dialogWindow = builder.build();
        EntityInspectorDetailView view = (EntityInspectorDetailView) dialogWindow.getView();

        view.setMetadataClassName(entityMetaClass);
        view.setMetadataId(entityId);
        view.setParentDataContext(parentDataContext);
        view.setParentProperty(parentProperty);
        view.setParentDataContainer(parentDataContainer);

        if (afterSaveHandler != null) {
            dialogWindow.addAfterCloseListener(event -> {
                if (event.closedWith(StandardOutcome.SAVE)
                        && event.getView() instanceof DetailView) {
                    E savedEntity = ((DetailView<E>) event.getView()).getEditedEntity();
                    afterSaveHandler.accept(savedEntity);
                }
            });
        }

        dialogWindow.open();
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public void setEntityMetaClass(String entityMetaClass) {
        this.entityMetaClass = entityMetaClass;
    }

    public void setParentDataContext(DataContext parentDataContext) {
        this.parentDataContext = parentDataContext;
    }

    public void setParentProperty(String parentProperty) {
        this.parentProperty = parentProperty;
    }

    public void setParentDataContainer(CollectionContainer<Object> parentDataContainer) {
        this.parentDataContainer = parentDataContainer;
    }

    public EntityInspectorEditAction<E> withEntityId(String entityId) {
        this.entityId = entityId;
        return this;
    }

    public EntityInspectorEditAction<E> withEntityMetaClass(String entityMetaClass) {
        this.entityMetaClass = entityMetaClass;
        return this;
    }

    public EntityInspectorEditAction<E> withParentDataContext(DataContext parentDataContext) {
        this.parentDataContext = parentDataContext;
        return this;
    }

    public EntityInspectorEditAction<E> withParentProperty(String parentProperty) {
        this.parentProperty = parentProperty;
        return this;
    }

    public EntityInspectorEditAction<E> withParentDataContainer(CollectionContainer<Object> parentDataContainer) {
        this.parentDataContainer = parentDataContainer;
        return this;
    }
}

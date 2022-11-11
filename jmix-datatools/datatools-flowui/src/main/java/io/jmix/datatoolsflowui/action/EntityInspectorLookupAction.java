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

import io.jmix.core.DevelopmentException;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.datatoolsflowui.view.entityinspector.EntityInspectorListView;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.entitypicker.EntityLookupAction;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.builder.LookupWindowBuilder;

@ActionType(EntityInspectorLookupAction.ID)
public class EntityInspectorLookupAction<E> extends EntityLookupAction<E> {

    public static final String ID = "entity_inspector_lookup";

    protected String entityName;

    public EntityInspectorLookupAction() {
        this(ID);
    }

    public EntityInspectorLookupAction(String id) {
        super(id);
    }

    @Override
    public void execute() {
        MetaClass metaClass = target.getMetaClass();
        if (metaClass == null) {
            throw new DevelopmentException("Neither metaClass nor dataContainer/property is specified " +
                    "for the " + target.getClass().getSimpleName(), "action ID", getId());
        }

        LookupWindowBuilder<E, View<?>> builder = dialogWindows.lookup(target);

        builder = viewInitializer.initWindowBuilder(builder);

        if (selectValidator != null) {
            builder = builder.withSelectValidator(selectValidator);
        }

        if (transformation != null) {
            builder = builder.withTransformation(transformation);
        }

        DialogWindow<View<?>> dialogWindow = builder.build();

        if (entityName != null) {
            ((EntityInspectorListView) dialogWindow.getView()).setEntityName(entityName);
        }

        dialogWindow.open();
    }

    public String getEntityNameParameter() {
        return entityName;
    }

    public void setEntityNameParameter(String entityName) {
        this.entityName = entityName;
    }

    public EntityInspectorLookupAction<E> withEntityNameParameter(String entityName) {
        this.entityName = entityName;
        return this;
    }
}

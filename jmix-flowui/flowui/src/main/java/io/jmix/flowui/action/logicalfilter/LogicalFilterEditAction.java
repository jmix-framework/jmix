/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.action.logicalfilter;

import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.list.EditAction;
import io.jmix.flowui.app.filter.condition.FilterConditionDetailView;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.entity.filter.FilterCondition;
import io.jmix.flowui.view.DetailView;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.builder.DetailWindowBuilder;

@ActionType(LogicalFilterEditAction.ID)
public class LogicalFilterEditAction extends EditAction<FilterCondition> {

    public static final String ID = "logicalFilter_edit";

    @SuppressWarnings("unused")
    public LogicalFilterEditAction() {
        this(ID);
    }

    public LogicalFilterEditAction(String id) {
        super(id);
    }

    protected Configuration configuration;

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void openDialog(FilterCondition editedEntity) {
        DetailWindowBuilder<FilterCondition, View<?>> builder = dialogWindows.detail(target);

        builder = viewInitializer.initWindowBuilder(builder);

        builder = builder.editEntity(editedEntity);

        if (transformation != null) {
            builder = builder.withTransformation(transformation);
        }

        DialogWindow<View<?>> dialogWindow = builder.build();
        if (afterSaveHandler != null) {
            dialogWindow.addAfterCloseListener(event -> {
                if (event.closedWith(StandardOutcome.SAVE)
                        && event.getView() instanceof DetailView) {
                    FilterCondition savedEntity = ((DetailView<FilterCondition>) event.getView()).getEditedEntity();
                    afterSaveHandler.accept(savedEntity);
                }
            });
        }

        if (configuration != null) {
            ((FilterConditionDetailView<?>) dialogWindow.getView()).setCurrentConfiguration(configuration);
        }

        dialogWindow.open();
    }
}

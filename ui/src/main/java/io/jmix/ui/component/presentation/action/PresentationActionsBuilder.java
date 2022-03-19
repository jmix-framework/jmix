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

package io.jmix.ui.component.presentation.action;

import io.jmix.core.AccessManager;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.component.Table;
import io.jmix.ui.accesscontext.UiGlobalPresentationContext;
import io.jmix.ui.presentation.TablePresentations;
import io.jmix.ui.presentation.model.TablePresentation;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@Component("ui_PresentationActionsBuilder")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PresentationActionsBuilder {

    public enum Type {
        SAVE,
        SAVE_AS,
        EDIT,
        DELETE,
        RESET
    }

    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected ApplicationContext applicationContext;

    protected Table table;
    protected Collection actionTypes;
    protected ComponentSettingsBinder settingsBinder;

    public PresentationActionsBuilder(Table component, ComponentSettingsBinder settingsBinder) {
        table = component;
        this.settingsBinder = settingsBinder;
    }

    public Collection<AbstractAction> build() {
        Collection<AbstractAction> actions = new ArrayList<>();
        AutowireCapableBeanFactory factory = applicationContext.getAutowireCapableBeanFactory();
        for (Object type : getActionTypes()) {
            AbstractAction action = buildAction(type);
            if (action != null) {
                factory.autowireBean(action);
                actions.add(action);
            }
        }
        return actions;
    }

    @Nullable
    public AbstractAction buildAction(@Nullable Object type) {
        if (type == null)
            return null;
        return buildActionByType(type);
    }

    public Collection getActionTypes() {
        if (actionTypes == null)
            actionTypes = Arrays.asList(Type.values());
        return actionTypes;
    }

    @SuppressWarnings("unused")
    public void setActionTypes(Collection actionTypes) {
        this.actionTypes = actionTypes;
    }

    @Nullable
    protected AbstractAction buildActionByType(Object type) {
        if (type instanceof Type) {
            switch ((Type) type) {
                case SAVE:
                    return buildSaveAction();
                case SAVE_AS:
                    return buildSaveAsAction();
                case EDIT:
                    return buildEditAction();
                case DELETE:
                    return buildDeleteAction();
                case RESET:
                    return buildResetAction();
            }
        }
        return buildCustomAction(type);
    }

    @SuppressWarnings("unused")
    @Nullable
    protected AbstractAction buildCustomAction(Object type) {
        return null;
    }

    @Nullable
    protected AbstractAction buildSaveAction() {
        if (isGlobalPresentation())
            return new SavePresentationAction(table, settingsBinder);
        return null;
    }

    protected AbstractAction buildSaveAsAction() {
        return new SaveAsPresentationAction(table, settingsBinder);
    }

    @Nullable
    protected AbstractAction buildEditAction() {
        if (isGlobalPresentation())
            return new EditPresentationAction(table, settingsBinder);
        return null;
    }

    @Nullable
    protected AbstractAction buildDeleteAction() {
        if (isGlobalPresentation())
            return new DeletePresentationAction(table);
        return null;
    }

    protected AbstractAction buildResetAction() {
        return new ResetPresentationAction(table);
    }

    protected boolean isGlobalPresentation() {
        TablePresentations presentations = table.getPresentations();
        TablePresentation presentation = presentations.getCurrent();

        UiGlobalPresentationContext globalPresentationContext = new UiGlobalPresentationContext();
        accessManager.applyRegisteredConstraints(globalPresentationContext);

        return presentation != null && (!presentations.isGlobal(presentation) ||
                globalPresentationContext.isPermitted());
    }
}

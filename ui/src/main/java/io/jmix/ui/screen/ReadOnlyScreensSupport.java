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

package io.jmix.ui.screen;

import io.jmix.core.AccessManager;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.ActionsHolder;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Component.Editable;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.data.HasValueSource;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.accesscontext.UiEntityAttributeContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.function.Predicate;

import static io.jmix.ui.screen.EditorScreen.ENABLE_EDITING;
import static io.jmix.ui.screen.EditorScreen.WINDOW_CLOSE;

/**
 * Bean that encapsulates the default logic of changing screens read-only mode.
 */
@org.springframework.stereotype.Component("ui_ReadOnlyScreensSupport")
public class ReadOnlyScreensSupport {

    protected AccessManager accessManager;

    @Autowired
    public void secAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    /**
     * Changes the read-only mode of the given screen.
     * <p>
     * The following components and actions change their state:
     * <ul>
     *     <li>All {@link Editable} components that has a not null {@link ValueSource}</li>
     *     <li>All {@link Action.AdjustWhenScreenReadOnly} actions obtained from {@link ActionsHolder} components</li>
     *     <li>All own screen actions except {@link EditorScreen#WINDOW_CLOSE}
     *     and {@link EditorScreen#ENABLE_EDITING}</li>
     * </ul>
     * <p>
     *
     * @param screen   a screen to set the read-only mode
     * @param readOnly whether a screen in the read-only mode
     */
    public void setScreenReadOnly(Screen screen, boolean readOnly) {
        setScreenReadOnly(screen, readOnly, true);
    }

    /**
     * Changes the read-only mode of the given screen.
     * <p>
     * The following components and actions change their state:
     * <ul>
     *     <li>All {@link Editable} components that has a not null {@link ValueSource}</li>
     *     <li>All {@link Action.AdjustWhenScreenReadOnly} actions obtained from {@link ActionsHolder} components</li>
     *     <li>All own screen actions except {@link EditorScreen#WINDOW_CLOSE}
     *     and {@link EditorScreen#ENABLE_EDITING}</li>
     * </ul>
     * <p>
     *
     * @param screen               a screen to set the read-only mode
     * @param readOnly             whether a screen in the read-only mode
     * @param showEnableEditingBtn whether or not the {@link EditorScreen#ENABLE_EDITING}
     *                             should be displayed in the read-only mode
     */
    public void setScreenReadOnly(Screen screen, boolean readOnly, boolean showEnableEditingBtn) {
        updateComponentsEditableState(screen, readOnly);
        updateOwnActionsEnableState(screen, readOnly);

        Action action = screen.getWindow().getAction(ENABLE_EDITING);
        if (action != null) {
            action.setVisible(showEnableEditingBtn && readOnly);
        }
    }

    protected void updateComponentsEditableState(Screen screen, boolean readOnly) {
        ComponentsHelper.walkComponents(screen.getWindow(), (component, name) -> {
            if (component instanceof Editable
                    && isChangeEditable(component)) {
                boolean editable = isEditableConsideringDataBinding(component, !readOnly);
                ((Editable) component).setEditable(editable);
            }

            if (component instanceof ActionsHolder) {
                updateActionsEnableState(((ActionsHolder) component).getActions(), readOnly,
                        this::isChangeComponentActionEnabled);
            }
        });
    }

    protected boolean isEditableConsideringDataBinding(Component component, boolean editable) {
        boolean shouldBeEditable = true;

        if (component instanceof HasValueSource
                && ((HasValueSource) component).getValueSource() != null) {
            ValueSource valueSource = ((HasValueSource) component).getValueSource();
            shouldBeEditable = !valueSource.isReadOnly();

            if (valueSource instanceof EntityValueSource
                    && ((EntityValueSource) valueSource).isDataModelSecurityEnabled()) {
                MetaPropertyPath metaPropertyPath = ((EntityValueSource) valueSource).getMetaPropertyPath();

                UiEntityAttributeContext attributeContext = new UiEntityAttributeContext(metaPropertyPath);
                accessManager.applyRegisteredConstraints(attributeContext);

                if (!attributeContext.canModify()
                        || !attributeContext.canView()) {
                    shouldBeEditable = false;
                }
            }
        }

        return editable && shouldBeEditable;
    }

    protected boolean isChangeComponentActionEnabled(Action action) {
        return action instanceof Action.AdjustWhenScreenReadOnly
                && ((Action.AdjustWhenScreenReadOnly) action).isDisabledWhenScreenReadOnly();
    }

    protected boolean isChangeOwnActionEnabled(Action action) {
        return !WINDOW_CLOSE.equals(action.getId())
                && !ENABLE_EDITING.equals(action.getId());
    }

    protected boolean isChangeEditable(Component component) {
        return component instanceof HasValueSource
                && ((HasValueSource) component).getValueSource() != null;
    }

    protected void updateOwnActionsEnableState(Screen screen, boolean readOnly) {
        updateActionsEnableState(screen.getWindow().getActions(), readOnly, this::isChangeOwnActionEnabled);
    }

    protected void updateActionsEnableState(Collection<Action> actions, boolean readOnly,
                                            Predicate<Action> shouldChangeEnabled) {
        actions.stream()
                .filter(shouldChangeEnabled)
                .forEach(action ->
                        action.setEnabled(!readOnly));
    }
}

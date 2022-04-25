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

package io.jmix.flowui.screen;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValueAndElement;
import io.jmix.core.AccessManager;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.SimilarToUi;
import io.jmix.flowui.action.AdjustWhenScreenReadOnly;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.component.HasActions;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.kit.action.Action;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Bean that encapsulates the default logic of changing screens read-only mode.
 */
@SimilarToUi
@org.springframework.stereotype.Component("flowui_ReadOnlyScreensSupport")
public class ReadOnlyScreensSupport {

    protected AccessManager accessManager;

    @Autowired
    public ReadOnlyScreensSupport(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    /**
     * Changes the read-only mode of the given screen.
     * <p>
     * The following components change their state:
     * <ul>
     *     <li>All {@link HasValueAndElement} components that has a not null {@link ValueSource}</li>
     *     <li>All {@link AdjustWhenScreenReadOnly} actions obtained from {@link HasActions} components</li>
     * </ul>
     * All own screen actions will be refreshed ({@link Action#refreshState()}).
     * <p>
     *
     * @param screen   a screen to set the read-only mode
     * @param readOnly whether a screen in the read-only mode
     */
    public void setScreenReadOnly(Screen screen, boolean readOnly) {
        Preconditions.checkNotNullArgument(screen);

        updateComponentsReadOnlyState(screen, readOnly);
        refreshOwnActionStates(screen);
    }

    protected void updateComponentsReadOnlyState(Screen screen, boolean readOnly) {
        for (Component component : UiComponentUtils.getComponents(screen.getContent())) {
            if (component instanceof HasValueAndElement
                    && isChangeReadOnly(component)) {
                boolean editable = isEditableConsideringDataBinding(component, !readOnly);
                ((HasValueAndElement<?, ?>) component).setReadOnly(!editable);
            }
            if (component instanceof HasActions) {
                updateActionsEnableState(((HasActions) component).getActions(), readOnly,
                        this::isChangeComponentActionEnabled);
            }
        }
    }

    protected boolean isEditableConsideringDataBinding(Component component, boolean editable) {
        boolean shouldBeEditable = true;

        if (component instanceof SupportsValueSource) {
            ValueSource<?> valueSource = ((SupportsValueSource<?>) component).getValueSource();
            if (valueSource != null) {
                shouldBeEditable = !valueSource.isReadOnly();

                // todo security
                /*if (valueSource instanceof EntityValueSource
                        && ((EntityValueSource) valueSource).isDataModelSecurityEnabled()) {
                    MetaPropertyPath metaPropertyPath = ((EntityValueSource) valueSource).getMetaPropertyPath();

                    UiEntityAttributeContext attributeContext = new UiEntityAttributeContext(metaPropertyPath);
                    accessManager.applyRegisteredConstraints(attributeContext);

                    if (!attributeContext.canModify()
                            || !attributeContext.canView()) {
                        shouldBeEditable = false;
                    }
                }*/
            }
        }

        return editable && shouldBeEditable;
    }

    protected boolean isChangeComponentActionEnabled(Action action) {
        return action instanceof AdjustWhenScreenReadOnly
                && ((AdjustWhenScreenReadOnly) action).isDisabledWhenScreenReadOnly();
    }

    protected boolean isChangeReadOnly(Component component) {
        return component instanceof SupportsValueSource
                && ((SupportsValueSource<?>) component).getValueSource() != null;
    }

    protected void refreshOwnActionStates(Screen screen) {
        screen.getScreenActions().getActions().forEach(Action::refreshState);
    }

    protected void updateActionsEnableState(Collection<Action> actions, boolean readOnly,
                                            Predicate<Action> shouldChangeEnabled) {
        actions.stream()
                .filter(shouldChangeEnabled)
                .forEach(action -> action.setEnabled(!readOnly));
    }
}

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

package io.jmix.flowui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValueAndElement;
import io.jmix.core.AccessManager;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.accesscontext.FlowuiEntityAttributeContext;
import io.jmix.flowui.action.AdjustWhenViewReadOnly;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.data.EntityValueSource;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasActions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Bean that encapsulates the default logic of changing views read-only mode.
 */
@org.springframework.stereotype.Component("flowui_ReadOnlyViewsSupport")
public class ReadOnlyViewsSupport {

    protected AccessManager accessManager;

    @Autowired
    public ReadOnlyViewsSupport(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    /**
     * Changes the read-only mode of the given view.
     * <p>
     * The following components change their state:
     * <ul>
     *     <li>All {@link HasValueAndElement} components that has a not null {@link ValueSource}</li>
     *     <li>All {@link AdjustWhenViewReadOnly} actions obtained from {@link HasActions} components</li>
     * </ul>
     * All own view actions will be refreshed ({@link Action#refreshState()}).
     * <p>
     *
     * @param view     a view to set the read-only mode
     * @param readOnly whether a view in the read-only mode
     */
    public void setViewReadOnly(View<?> view, boolean readOnly) {
        Preconditions.checkNotNullArgument(view);

        updateComponentsReadOnlyState(view, readOnly);
        refreshOwnActionStates(view);
    }

    protected void updateComponentsReadOnlyState(View<?> view, boolean readOnly) {
        Component content = view.getContent();
        if (!UiComponentUtils.isContainer(content)) {
            return;
        }

        for (Component component : UiComponentUtils.getComponents(content)) {
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

                if (valueSource instanceof EntityValueSource
                        && ((EntityValueSource<?, ?>) valueSource).isDataModelSecurityEnabled()) {
                    MetaPropertyPath metaPropertyPath = ((EntityValueSource<?, ?>) valueSource).getMetaPropertyPath();

                    FlowuiEntityAttributeContext attributeContext = new FlowuiEntityAttributeContext(metaPropertyPath);
                    accessManager.applyRegisteredConstraints(attributeContext);

                    if (!attributeContext.canModify()
                            || !attributeContext.canView()) {
                        shouldBeEditable = false;
                    }
                }
            }
        }

        return editable && shouldBeEditable;
    }

    protected boolean isChangeComponentActionEnabled(Action action) {
        return action instanceof AdjustWhenViewReadOnly
                && ((AdjustWhenViewReadOnly) action).isDisabledWhenViewReadOnly();
    }

    protected boolean isChangeReadOnly(Component component) {
        return component instanceof SupportsValueSource
                && ((SupportsValueSource<?>) component).getValueSource() != null;
    }

    protected void refreshOwnActionStates(View<?> view) {
        view.getViewActions().getActions().forEach(Action::refreshState);
    }

    protected void updateActionsEnableState(Collection<Action> actions, boolean readOnly,
                                            Predicate<Action> shouldChangeEnabled) {
        actions.stream()
                .filter(shouldChangeEnabled)
                .forEach(action -> action.setEnabled(!readOnly));
    }
}

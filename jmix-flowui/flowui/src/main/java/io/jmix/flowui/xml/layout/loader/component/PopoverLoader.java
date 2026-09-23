/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.loader.container.AbstractContainerLoader;

public class PopoverLoader extends AbstractContainerLoader<Popover> {

    @Override
    protected Popover createComponent() {
        return factory.create(Popover.class);
    }

    @Override
    public void initComponent() {
        super.initComponent();

        createSubComponents(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        loadEnum(element, PopoverPosition.class, "position", resultComponent::setPosition);
        loadBoolean(element, "modal", resultComponent::setModal);
        loadBoolean(element, "backdropVisible", resultComponent::setBackdropVisible);
        loadBoolean(element, "autofocus", resultComponent::setAutofocus);
        loadBoolean(element, "tabFocusEnabled", resultComponent::setTabFocusEnabled);
        loadBoolean(element, "closeOnEsc", resultComponent::setCloseOnEsc);
        loadBoolean(element, "closeOnOutsideClick", resultComponent::setCloseOnOutsideClick);
        loadBoolean(element, "openOnClick", resultComponent::setOpenOnClick);
        loadBoolean(element, "openOnFocus", resultComponent::setOpenOnFocus);
        loadBoolean(element, "openOnHover", resultComponent::setOpenOnHover);
        loadInteger(element, "focusDelay", resultComponent::setFocusDelay);
        loadInteger(element, "hoverDelay", resultComponent::setHoverDelay);
        loadInteger(element, "hideDelay", resultComponent::setHideDelay);
        loadString(element, "role", resultComponent::setRole);

        // Popover is not HasSize: setWidth/setHeight write element properties that the web component
        // forwards to the teleported overlay, and there is no min/max size equivalent.
        loadString(element, "width", resultComponent::setWidth);
        loadString(element, "height", resultComponent::setHeight);

        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadAriaLabel(resultComponent, element);

        // Pre-init, not init: pre-init tasks run before DependencyManager, so @Subscribe handlers
        // see a bound popover.
        loadString(element, "target")
                .ifPresent(targetId -> getContext().addPreInitTask(new FindPopoverTargetTask(targetId)));

        loadSubComponents();
    }

    protected class FindPopoverTargetTask implements InitTask {

        protected final String targetId;

        public FindPopoverTargetTask(String targetId) {
            this.targetId = targetId;
        }

        @Override
        public void execute(Context context) {
            Component origin = null;
            if (context instanceof ComponentContext componentContext) {
                origin = componentContext.getView();
            } else if (context instanceof FragmentContext fragmentContext) {
                origin = fragmentContext.getFragment();
            }

            if (origin == null) {
                return;
            }

            Component target = UiComponentUtils.findComponent(origin, targetId)
                    .orElseThrow(() -> new GuiDevelopmentException(
                            "Component with the '" + targetId + "' ID is not found", context));

            resultComponent.setTarget(target);
        }
    }
}

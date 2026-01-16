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
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.sidepanellayout.SidePanelLayout;
import io.jmix.flowui.component.sidepanellayout.SidePanelLayoutToggle;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.IconLoaderSupport;

import static com.vaadin.flow.dom.ElementConstants.ARIA_LABEL_ATTRIBUTE_NAME;

public class SidePanelLayoutToggleLoader extends AbstractComponentLoader<SidePanelLayoutToggle> {

    protected IconLoaderSupport iconLoaderSupport;

    @Override
    protected SidePanelLayoutToggle createComponent() {
        return factory.create(SidePanelLayoutToggle.class);
    }

    @Override
    public void loadComponent() {
        getLoaderSupport().loadResourceString(element, "ariaLabel",
                getContext().getMessageGroup(), ariaLabel ->
                        resultComponent.getElement().setAttribute(ARIA_LABEL_ATTRIBUTE_NAME, ariaLabel));

        iconLoaderSupport().loadIcon(element, resultComponent::setIcon);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadFocusableAttributes(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadAriaLabel(resultComponent, element);
        componentLoader().loadClickNotifierAttributes(resultComponent, element);

        loadString(element, "sidePanelLayoutId")
                .ifPresentOrElse(
                        id -> {
                            if (getContext() instanceof ComponentContext componentContext) {
                                componentContext.addPreInitTask(new FindSidePanelLayoutTask(id));
                            } else if (getContext() instanceof FragmentContext fragmentContext) {
                                // TODO: pinyazhin, fragment's pre-init
                                fragmentContext.addInitTask(new FindSidePanelLayoutTask(id));
                            }
                        },
                        () -> {
                            if (getContext() instanceof ComponentContext componentContext) {
                                componentContext.addPreInitTask(new FindSidePanelLayoutToggleParentTask());
                            } else if (getContext() instanceof FragmentContext fragmentContext) {
                                // TODO: pinyazhin, fragment's pre-init
                                fragmentContext.addInitTask(new FindSidePanelLayoutToggleParentTask());
                            }
                        }
                );
    }

    protected IconLoaderSupport iconLoaderSupport() {
        if (iconLoaderSupport == null) {
            iconLoaderSupport = applicationContext.getBean(IconLoaderSupport.class, context);
        }

        return iconLoaderSupport;
    }

    protected class FindSidePanelLayoutTask implements InitTask {

        protected String drawerLayoutId;

        public FindSidePanelLayoutTask(String drawerLayoutId) {
            this.drawerLayoutId = drawerLayoutId;
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

            Component component = UiComponentUtils.findComponent(origin, drawerLayoutId).orElse(null);
            if (component == null) {
                throw new GuiDevelopmentException("Component with the '" + drawerLayoutId + "' ID is not found",
                        context);
            }
            if (component instanceof SidePanelLayout drawerLayout) {
                resultComponent.setSidePanelLayout(drawerLayout);
            } else {
                throw new GuiDevelopmentException("Component with the '" + drawerLayoutId +
                        "' ID is not the " + SidePanelLayout.class.getSimpleName() + " class or its subclass", context);
            }
        }
    }

    protected class FindSidePanelLayoutToggleParentTask implements InitTask {

        @Override
        public void execute(Context context) {
            Component parent = resultComponent.getParent().orElse(null);
            if (parent == null) {
                return;
            }
            while (parent != null) {
                if (parent instanceof SidePanelLayout drawerLayout) {
                    resultComponent.setSidePanelLayout(drawerLayout);
                    return;
                }
                parent = parent.getParent().orElse(null);
            }

            throw new GuiDevelopmentException(SidePanelLayoutToggle.class.getSimpleName()
                    + " component must be placed inside "
                    + SidePanelLayout.class.getSimpleName() +
                    " or define 'sidePanelLayoutId' attribute", context);
        }
    }
}

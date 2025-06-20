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

package io.jmix.flowui.component.valuepicker;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.valuepicker.PickerAction;
import io.jmix.flowui.component.PickerComponent;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.valuepicker.ValuePickerActionSupport;
import io.jmix.flowui.kit.component.valuepicker.ValuePickerButton;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;

@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@org.springframework.stereotype.Component("flowui_JmixValuePickerActionSupport")
public class JmixValuePickerActionSupport extends ValuePickerActionSupport implements ApplicationContextAware {

    protected ApplicationContext applicationContext;

    /**
     * @deprecated use one of {@link JmixValuePickerActionSupport#JmixValuePickerActionSupport(Component)},
     * {@link JmixValuePickerActionSupport#JmixValuePickerActionSupport(Component, String, String)}
     */
    @Deprecated(since = "2.2", forRemoval = true)
    public JmixValuePickerActionSupport(HasElement component) {
        //noinspection removal
        super(component);
    }

    /**
     * @deprecated use one of {@link JmixValuePickerActionSupport#JmixValuePickerActionSupport(Component)},
     * {@link JmixValuePickerActionSupport#JmixValuePickerActionSupport(Component, String, String)}
     */
    @Deprecated(since = "2.2", forRemoval = true)
    public JmixValuePickerActionSupport(PickerComponent<?> component,
                                        String actionsSlot, String hasActionsAttribute) {
        //noinspection removal
        super(component, actionsSlot, hasActionsAttribute);
    }

    public <C extends Component & PickerComponent<?>> JmixValuePickerActionSupport(C component) {
        super(component);
    }

    public <C extends Component & PickerComponent<?>> JmixValuePickerActionSupport(C component,
                                                                                   String actionsSlot,
                                                                                   String hasActionsAttribute) {
        super(component, actionsSlot, hasActionsAttribute);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void attachAction(Action action) {
        super.attachAction(action);

        if (action instanceof PickerAction) {
            ((PickerAction<?, PickerComponent<?>, ?>) action)
                    .setTarget(((PickerComponent<?>) component));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void detachAction(Action action) {
        super.detachAction(action);

        if (action instanceof PickerAction) {
            ((PickerAction<?, PickerComponent<?>, ?>) action)
                    .setTarget(null);
        }
    }

    @Override
    protected ValuePickerButton createButton() {
        // for backward capability
        if (applicationContext != null) {
            return applicationContext.getBean(UiComponents.class).create(ValuePickerButton.class);
        }

        return super.createButton();
    }
}

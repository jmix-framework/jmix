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

package io.jmix.flowui.action.security;

import com.vaadin.flow.component.Component;
import io.jmix.core.Messages;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.ObservableBaseAction;
import io.jmix.flowui.icon.Icons;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import io.jmix.flowui.sys.LogoutSupport;
import io.micrometer.observation.Observation;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@ActionType(LogoutAction.ID)
public class LogoutAction extends ObservableBaseAction<LogoutAction> implements ExecutableAction, ApplicationContextAware {

    public static final String ID = "logout";

    protected ApplicationContext applicationContext;

    public LogoutAction(String id) {
        super(id);

        initAction();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    protected void initAction() {
        // hook to be implemented
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.description = messages.getMessage("actions.logout.description");
    }

    @Autowired
    protected void setIcons(Icons icons) {
        // Check for 'null' for backward compatibility because 'icon' can be set in
        // the 'initAction()' method which is called before injection.
        if (this.icon == null) {
            this.icon = icons.get(JmixFontIcon.LOGOUT_ACTION);
        }
    }

    @Override
    public void actionPerform(Component component) {
        // if standard behaviour
        if (!hasListener(ActionPerformedEvent.class)) {
            getUiObservationSupport()
                    .map(support -> support.createActionExeutionObservation(this))
                    .orElse(Observation.NOOP)
                    .observe(this::execute);
        } else {
            super.actionPerform(component);
        }
    }

    @Override
    public void execute() {
        applicationContext.getBean(LogoutSupport.class).logout();
    }
}

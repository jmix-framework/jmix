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
import io.jmix.flowui.UiIconProperties;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.sys.LogoutSupport;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@ActionType(LogoutAction.ID)
public class LogoutAction extends BaseAction implements ExecutableAction, ApplicationContextAware {

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
        // hook to be implemented, keep for backward compatibility
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.description = messages.getMessage("actions.logout.description");
    }

    @Autowired
    protected void setUiIconProperties(UiIconProperties uiIconProperties) {
        // For backward compatibility, set the default icon only if the icon is null,
        // i.e., it was not set in the 'initAction' method, which is called first.
        if (icon == null) {
            this.icon = ComponentUtils.parseIcon(uiIconProperties.getLogoutIcon());
        }
    }

    @Override
    public void actionPerform(Component component) {
        // if standard behaviour
        if (!hasListener(ActionPerformedEvent.class)) {
            execute();
        } else {
            super.actionPerform(component);
        }
    }

    @Override
    public void execute() {
        applicationContext.getBean(LogoutSupport.class).logout();
    }
}

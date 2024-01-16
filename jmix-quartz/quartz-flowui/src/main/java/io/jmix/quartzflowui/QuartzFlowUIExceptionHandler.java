/*
 * Copyright 2024 Haulmont.
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

package io.jmix.quartzflowui;

import com.vaadin.flow.component.notification.Notification;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiViewProperties;
import io.jmix.flowui.exception.AbstractUiExceptionHandler;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.quartz.exception.QuartzJobSaveException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component 
public class QuartzFlowUIExceptionHandler extends AbstractUiExceptionHandler {
    @Autowired
    private final Notifications notifications;
    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected UiViewProperties viewProperties;
    public QuartzFlowUIExceptionHandler(Notifications notifications) {
        super(QuartzJobSaveException.class.getName());
        this.notifications = notifications;
    }

    @Override
    protected void doHandle(String className, String message, Throwable throwable) {
        messageBundle.setMessageGroup("io.jmix.quartzflowui.view.template");
        notifications.create(messageBundle.getMessage("notification.template.validation.header"), message)
                .withDuration(viewProperties.getValidationNotificationDuration())
                .withPosition(Notification.Position.valueOf(viewProperties.getValidationNotificationPosition()))
                .withType(Notifications.Type.valueOf(viewProperties.getValidationNotificationType()))
                .show();
    }
}
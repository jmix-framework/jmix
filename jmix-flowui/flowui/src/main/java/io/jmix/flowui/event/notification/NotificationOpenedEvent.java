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

package io.jmix.flowui.event.notification;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.notification.Notification;
import io.jmix.flowui.Notifications;
import org.springframework.context.ApplicationEvent;

/**
 * Application event that is sent after the notification is shown.
 */
public class NotificationOpenedEvent extends ApplicationEvent {

    protected String text;
    protected String title;
    protected String message;
    protected Component component;

    protected Notifications.Type type;

    public NotificationOpenedEvent(Notification notification, String text, String title, String message,
                                   Component component, Notifications.Type type) {
        super(notification);

        this.text = text;
        this.title = title;
        this.message = message;
        this.component = component;

        this.type = type;
    }

    @Override
    public Notification getSource() {
        return ((Notification) super.getSource());
    }

    /**
     * @return the {@link Notification} text
     */
    public String getText() {
        return text;
    }

    /**
     * @return the {@link Notification} title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the {@link Notification} message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the {@link Notification} component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * @return the {@link Notification} type
     */
    public Notifications.Type getType() {
        return type;
    }
}

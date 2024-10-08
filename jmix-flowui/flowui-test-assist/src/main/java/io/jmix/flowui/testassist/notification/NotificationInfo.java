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

package io.jmix.flowui.testassist.notification;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.notification.Notification;
import io.jmix.flowui.Notifications;

/**
 * POJO class for storing notification information.
 */
public class NotificationInfo {

    protected final Notification notification;

    protected String text;
    protected String title;
    protected String message;
    protected Component component;

    protected Notifications.Type type;

    /**
     * Creates a {@link NotificationInfo} of the passed {@link Notification}.
     *
     * @param notification {@link Notification} to create {@link NotificationInfo}
     */
    public NotificationInfo(Notification notification) {
        this.notification = notification;
    }

    /**
     * Sets the {@code text} of the {@link Notification}.
     *
     * @param text text to set
     * @return this
     */
    public NotificationInfo withText(String text) {
        this.text = text;
        return this;
    }

    /**
     * Sets the {@code title} of the {@link Notification}.
     *
     * @param title title to set
     * @return this
     */
    public NotificationInfo withTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the {@code message} of the {@link Notification}.
     *
     * @param message message to set
     * @return this
     */
    public NotificationInfo withMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Sets the {@code component} of the {@link Notification}.
     *
     * @param component component to set
     * @return this
     */
    public NotificationInfo withComponent(Component component) {
        this.component = component;
        return this;
    }

    /**
     * Sets the {@code type} of the {@link Notification}.
     *
     * @param type type to set
     * @return this
     */
    public NotificationInfo withType(Notifications.Type type) {
        this.type = type;
        return this;
    }

    /**
     * @return {@link Notification} instance
     */
    public Notification getNotification() {
        return notification;
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

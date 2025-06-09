/*
 * Copyright 2025 Haulmont.
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

package io.jmix.masquerade.condition;

import io.jmix.masquerade.component.Notification;

/**
 * Condition for checking the notification message of {@link Notification} web-element wrapper.
 */
public class NotificationMessage extends SpecificCondition {

    protected String message;

    public NotificationMessage(String message) {
        super("notificationMessage");
        this.message = message;
    }

    /**
     * @return message value
     */
    public String getValue() {
        return message;
    }

    @Override
    public String toString() {
        return "%s='%s'".formatted(getName(), getValue());
    }
}

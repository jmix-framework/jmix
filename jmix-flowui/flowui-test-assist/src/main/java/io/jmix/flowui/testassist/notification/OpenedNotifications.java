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

import com.google.common.collect.Iterables;
import com.vaadin.flow.component.notification.Notification;
import io.jmix.flowui.event.notification.NotificationClosedEvent;
import io.jmix.flowui.event.notification.NotificationOpenedEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.event.EventListener;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean contains opened {@link Notification}s in order of opening.
 * <p>
 * Example of the order in which notifications are stored::
 * <ul>
 *     <li>first opened notification has index {@code 0}</li>
 *     <li>seconds opened notification has index {@code 1}</li>
 *     <li>last opened notification has index {@code openedNotifications.size() - 1}</li>
 * </ul>
 */
@Component("flowui_OpenedNotifications")
public class OpenedNotifications {

    protected static Map<Notification, NotificationInfo> openedNotifications = new LinkedHashMap<>();

    /**
     * @return immutable list of {@link NotificationInfo}s
     */
    public List<NotificationInfo> getNotifications() {
        return List.copyOf(openedNotifications.values());
    }

    /**
     * @return the most recent opened {@link NotificationInfo} or {@code null} if no opened notifications
     */
    @Nullable
    public NotificationInfo getLastNotification() {
        return CollectionUtils.isEmpty(openedNotifications.values())
                ? null
                : Iterables.getLast(openedNotifications.values());
    }

    /**
     * Closes opened {@link Notification}s and removes them from the storage map.
     */
    public void closeOpenedNotifications() {
        Iterator<Notification> iterator = openedNotifications.keySet().iterator();

        while (iterator.hasNext()) {
            Notification notification = iterator.next();
            iterator.remove();
            notification.close();
        }
    }

    @EventListener
    protected void onNotificationOpened(NotificationOpenedEvent event) {
        Notification notification = event.getSource();
        openedNotifications.put(notification, mapToNotificationInfo(event));
    }

    @EventListener
    protected void onNotificationClosed(NotificationClosedEvent event) {
        Notification notification = event.getSource();
        openedNotifications.remove(notification);
    }

    protected NotificationInfo mapToNotificationInfo(NotificationOpenedEvent event) {
        return new NotificationInfo(event.getSource())
                .withText(event.getText())
                .withTitle(event.getTitle())
                .withMessage(event.getMessage())
                .withComponent(event.getComponent())
                .withType(event.getType());
    }
}

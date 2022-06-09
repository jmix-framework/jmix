/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.component;

import io.jmix.core.common.event.Subscription;
import io.jmix.ui.Notifications;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioFacet;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;

import javax.annotation.Nullable;
import javax.validation.constraints.PositiveOrZero;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Prepares and shows notifications.
 */
@StudioFacet(
        xmlElement = "notification",
        caption = "Notification",
        description = "Prepares and shows notifications",
        defaultProperty = "caption",
        category = "Facets",
        icon = "io/jmix/ui/icon/facet/notification.svg",
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/facets/notification-facet.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "id", type = PropertyType.COMPONENT_ID, required = true)
        }
)
public interface NotificationFacet extends Facet {

    /**
     * Sets notification caption.
     *
     * @param caption caption
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING)
    void setCaption(@Nullable String caption);

    /**
     * @return notification caption
     */
    @Nullable
    String getCaption();

    /**
     * Sets notification description.
     *
     * @param description description
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING)
    void setDescription(@Nullable String description);

    /**
     * @return notification description
     */
    @Nullable
    String getDescription();

    /**
     * Sets the delay before the notification disappears.
     *
     * @param delayMs the desired delay in milliseconds
     */
    @StudioProperty
    @PositiveOrZero
    void setDelay(int delayMs);

    /**
     * @return the delay before the notification disappears
     */
    int getDelay();

    /**
     * Sets notification type.
     *
     * @param type type
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "HUMANIZED")
    void setType(Notifications.NotificationType type);

    /**
     * @return notification type
     */
    Notifications.NotificationType getType();

    /**
     * Sets notification content mode.
     *
     * @param contentMode content mode
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "TEXT", options = {"HTML", "TEXT"})
    void setContentMode(ContentMode contentMode);

    /**
     * @return notification content mode
     */
    ContentMode getContentMode();

    /**
     * Sets notification style name.
     *
     * @param styleName style name
     */
    @StudioProperty(name = "stylename", type = PropertyType.CSS_CLASSNAME_LIST)
    void setStyleName(String styleName);

    /**
     * @return notification style name
     */
    @Nullable
    String getStyleName();

    /**
     * Sets notification position.
     *
     * @param position position
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "DEFAULT")
    void setPosition(Notifications.Position position);

    /**
     * @return notification position
     */
    Notifications.Position getPosition();

    /**
     * @return id of action that triggers notification
     */
    @Nullable
    String getActionTarget();

    /**
     * Sets that notification should be shown when action with id {@code actionId}
     * is performed.
     *
     * @param actionId action id
     */
    @StudioProperty(name = "onAction", type = PropertyType.COMPONENT_REF,
            options = "io.jmix.ui.action.Action")
    void setActionTarget(@Nullable String actionId);

    /**
     * @return id of button that triggers notification
     */
    @Nullable
    String getButtonTarget();

    /**
     * Sets that notification should be shown when button with id {@code actionId}
     * is clicked.
     *
     * @param buttonId button id
     */
    @StudioProperty(name = "onButton", type = PropertyType.COMPONENT_REF,
            options = "io.jmix.ui.component.Button")
    void setButtonTarget(@Nullable String buttonId);

    /**
     * Sets whether html sanitizer is enabled or not for notification content.
     *
     * @param htmlSanitizerEnabled specifies whether html sanitizer is enabled
     */
    @StudioProperty(type = PropertyType.BOOLEAN, defaultValue = "true")
    void setHtmlSanitizerEnabled(boolean htmlSanitizerEnabled);

    /**
     * @return html sanitizer is enabled for notification content
     */
    boolean isHtmlSanitizerEnabled();

    /**
     * Shows notification.
     */
    void show();

    /**
     * Sets the given {@code Supplier} as notification caption provider.
     *
     * @param captionProvider notification caption provider
     */
    void setCaptionProvider(@Nullable Supplier<String> captionProvider);

    /**
     * @return notification caption provider
     */
    @Nullable
    Supplier<String> getCaptionProvider();

    /**
     * Sets the given {@code Supplier} as notification description provider.
     *
     * @param descriptionProvider notification description provider
     */
    void setDescriptionProvider(@Nullable Supplier<String> descriptionProvider);

    /**
     * @return notification description provider
     */
    @Nullable
    Supplier<String> getDescriptionProvider();

    /**
     * Adds the given {@code Consumer} as notification {@link Notifications.CloseEvent} listener.
     *
     * @param listener close event listener
     * @return close event subscription
     */
    Subscription addCloseListener(Consumer<Notifications.CloseEvent> listener);
}

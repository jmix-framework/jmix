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

package io.jmix.flowui;

import com.vaadin.flow.component.notification.Notification.Position;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.flowui.view")
@ConstructorBinding
public class FlowuiViewProperties {

    String closeShortcut;
    String saveShortcut;
    boolean createActionAddsFirst;
    boolean useSaveConfirmation;

    /**
     * Standard view validation error notification variant. Should contain the name of enum value
     * {@link Notifications.Type}.
     */
    String validationNotificationType;

    /**
     * Standard view validation error notification position. Should contain the name of enum value
     * {@link Position}.
     */
    String validationNotificationPosition;

    /**
     * The duration in milliseconds to show the view validation error notification.
     */
    Integer validationNotificationDuration;

    /**
     * Whether LookupBuilder will reload entity after it is selected from lookup window if the selected entity
     * doesn't contain all required entity attributes.
     */
    boolean reloadUnfetchedAttributesFromLookupViews;

    public FlowuiViewProperties(
            String closeShortcut,
            String saveShortcut,
            @DefaultValue("true") boolean createActionAddsFirst,
            @DefaultValue("true") boolean useSaveConfirmation,
            @DefaultValue("DEFAULT") String validationNotificationType,
            @DefaultValue("BOTTOM_END") String validationNotificationPosition,
            @DefaultValue("3000") Integer validationNotificationDuration,
            @DefaultValue("true") boolean reloadUnfetchedAttributesFromLookupViews
    ) {
        this.closeShortcut = closeShortcut;
        this.saveShortcut = saveShortcut;
        this.createActionAddsFirst = createActionAddsFirst;
        this.useSaveConfirmation = useSaveConfirmation;
        this.validationNotificationType = validationNotificationType;
        this.validationNotificationPosition = validationNotificationPosition;
        this.validationNotificationDuration = validationNotificationDuration;
        this.reloadUnfetchedAttributesFromLookupViews = reloadUnfetchedAttributesFromLookupViews;
    }

    public String getCloseShortcut() {
        return closeShortcut;
    }

    public String getSaveShortcut() {
        return saveShortcut;
    }

    public boolean isCreateActionAddsFirst() {
        return createActionAddsFirst;
    }

    public boolean isUseSaveConfirmation() {
        return useSaveConfirmation;
    }

    /**
     * @see #validationNotificationType
     */
    public String getValidationNotificationType() {
        return validationNotificationType;
    }

    /**
     * @see #validationNotificationPosition
     */
    public String getValidationNotificationPosition() {
        return validationNotificationPosition;
    }

    /**
     * @see #validationNotificationDuration
     */
    public Integer getValidationNotificationDuration() {
        return validationNotificationDuration;
    }

    /**
     * @see #reloadUnfetchedAttributesFromLookupViews
     */
    public boolean isReloadUnfetchedAttributesFromLookupViews() {
        return reloadUnfetchedAttributesFromLookupViews;
    }
}
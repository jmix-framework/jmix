/*
 * Copyright 2021 Haulmont.
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

@ConfigurationProperties(prefix = "jmix.flowui.screen")
@ConstructorBinding
public class FlowUiScreenProperties {

    String closeShortcut;
    String commitShortcut;
    boolean createActionAddsFirst;
    boolean useSaveConfirmation;

    /**
     * Standard screen validation error notification variant. Should contain the name of enum value
     * {@link Notifications.Type}.
     */
    String validationNotificationType;

    /**
     * Standard screen validation error notification position. Should contain the name of enum value
     * {@link Position}.
     */
    String validationNotificationPosition;

    /**
     * The duration in milliseconds to show the screen validation error notification.
     */
    Integer validationNotificationDuration;

    /**
     * Whether LookupBuilder will reload entity after it is selected from lookup window if the selected entity
     * doesn't contain all required entity attributes.
     */
    boolean reloadUnfetchedAttributesFromLookupScreens;
//    boolean layoutAnalyzerEnabled;

    public FlowUiScreenProperties(
            @DefaultValue("ESCAPE") String closeShortcut,
            @DefaultValue("CONTROL-ENTER") String commitShortcut,
            @DefaultValue("true") boolean createActionAddsFirst,
            @DefaultValue("true") boolean useSaveConfirmation,
            @DefaultValue("DEFAULT") String validationNotificationType,
            @DefaultValue("BOTTOM_END") String validationNotificationPosition,
            @DefaultValue("3000") Integer validationNotificationDuration,
            @DefaultValue("true") boolean reloadUnfetchedAttributesFromLookupScreens
//            @DefaultValue("true") boolean layoutAnalyzerEnabled
    ) {
        this.closeShortcut = closeShortcut;
        this.commitShortcut = commitShortcut;
        this.createActionAddsFirst = createActionAddsFirst;
        this.useSaveConfirmation = useSaveConfirmation;
        this.validationNotificationType = validationNotificationType;
        this.validationNotificationPosition = validationNotificationPosition;
        this.validationNotificationDuration = validationNotificationDuration;
        this.reloadUnfetchedAttributesFromLookupScreens = reloadUnfetchedAttributesFromLookupScreens;
//        this.layoutAnalyzerEnabled = layoutAnalyzerEnabled;
    }

    public String getCloseShortcut() {
        return closeShortcut;
    }

    public String getCommitShortcut() {
        return commitShortcut;
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
     * @see #reloadUnfetchedAttributesFromLookupScreens
     */
    public boolean isReloadUnfetchedAttributesFromLookupScreens() {
        return reloadUnfetchedAttributesFromLookupScreens;
    }

    /*public boolean isLayoutAnalyzerEnabled() {
        return layoutAnalyzerEnabled;
    }*/
}

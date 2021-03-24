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

package io.jmix.ui;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.ui.screen")
@ConstructorBinding
public class UiScreenProperties {

    String closeShortcut;
    String commitShortcut;
    boolean createActionAddsFirst;
    boolean useSaveConfirmation;
    String validationNotificationType;
    boolean reloadUnfetchedAttributesFromLookupScreens;
    int maxTabCount;
    boolean showBreadCrumbs;
    boolean defaultScreenCanBeClosed;
    String defaultScreenId;
    String loginScreenId;
    String mainScreenId;
    String initialScreenId;
    boolean forceRefreshAuthenticatedTabs;
    boolean layoutAnalyzerEnabled;
    int mainTabCaptionLength;

    public UiScreenProperties(
            @DefaultValue("ESCAPE") String closeShortcut,
            @DefaultValue("CTRL-ENTER") String commitShortcut,
            @DefaultValue("true") boolean createActionAddsFirst,
            @DefaultValue("true") boolean useSaveConfirmation,
            @DefaultValue("TRAY") String validationNotificationType,
            @DefaultValue("true") boolean reloadUnfetchedAttributesFromLookupScreens,
            @DefaultValue("20") int maxTabCount,
            @DefaultValue("true") boolean showBreadCrumbs,
            @DefaultValue("true") boolean defaultScreenCanBeClosed,
            String defaultScreenId,
            @DefaultValue("login") String loginScreenId,
            @DefaultValue("main") String mainScreenId,
            String initialScreenId,
            @DefaultValue("false") boolean forceRefreshAuthenticatedTabs,
            @DefaultValue("true") boolean layoutAnalyzerEnabled,
            @DefaultValue("25") int mainTabCaptionLength
    ) {
        this.closeShortcut = closeShortcut;
        this.commitShortcut = commitShortcut;
        this.createActionAddsFirst = createActionAddsFirst;
        this.useSaveConfirmation = useSaveConfirmation;
        this.validationNotificationType = validationNotificationType;
        this.reloadUnfetchedAttributesFromLookupScreens = reloadUnfetchedAttributesFromLookupScreens;
        this.maxTabCount = maxTabCount;
        this.showBreadCrumbs = showBreadCrumbs;
        this.defaultScreenCanBeClosed = defaultScreenCanBeClosed;
        this.defaultScreenId = defaultScreenId;
        this.loginScreenId = loginScreenId;
        this.mainScreenId = mainScreenId;
        this.initialScreenId = initialScreenId;
        this.forceRefreshAuthenticatedTabs = forceRefreshAuthenticatedTabs;
        this.layoutAnalyzerEnabled = layoutAnalyzerEnabled;
        this.mainTabCaptionLength = mainTabCaptionLength;
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
     * Standard Window validation error notification type as one of Notifications.NotificationType values.
     */
    public String getValidationNotificationType() {
        return validationNotificationType;
    }

    /**
     * If true, then LookupBuilder will reload entity after it is selected from lookup window if the selected entity doesn't contain all
     * required entity attributes
     */
    public boolean isReloadUnfetchedAttributesFromLookupScreens() {
        return reloadUnfetchedAttributesFromLookupScreens;
    }

    /**
     * Maximum number of opened tabs. 0 for unlimited.
     */
    public int getMaxTabCount() {
        return maxTabCount;
    }

    /**
     * @return true if WindowBreadCrumbs is shown in screens, false - otherwise
     */
    public boolean isShowBreadCrumbs() {
        return showBreadCrumbs;
    }

    /**
     * Defines whether default screen can be closed or not when TABBED work area mode is used.
     */
    public boolean isDefaultScreenCanBeClosed() {
        return defaultScreenCanBeClosed;
    }

    /**
     * Defines which screen should be opened after login. This setting will be applied to all users.
     */
    public String getDefaultScreenId() {
        return defaultScreenId;
    }

    /**
     * Defines the screen that will be used as Login screen.
     *
     * @return the login screen id
     */
    public String getLoginScreenId() {
        return loginScreenId;
    }

    /**
     * Defines the screen that will be used as Main screen.
     *
     * @return the main screen id
     */
    public String getMainScreenId() {
        return mainScreenId;
    }

    /**
     * Defines the screen that will be open for non-authenticated user when an application opened.
     *
     * @return initial screen id
     */
    public String getInitialScreenId() {
        return initialScreenId;
    }

    /**
     * @return true if app should perform force refresh for browser tabs with authenticated sessions
     * or false otherwise
     */
    public boolean isForceRefreshAuthenticatedTabs() {
        return forceRefreshAuthenticatedTabs;
    }

    public boolean isLayoutAnalyzerEnabled() {
        return layoutAnalyzerEnabled;
    }

    /**
     * Maximum number of symbols in main tabs captions.
     */
    public int getMainTabCaptionLength() {
        return mainTabCaptionLength;
    }
}

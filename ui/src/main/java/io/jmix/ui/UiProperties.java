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

package io.jmix.ui;

import io.jmix.ui.navigation.UrlHandlingMode;
import io.jmix.ui.sanitizer.HtmlSanitizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties(prefix = "jmix.ui")
@ConstructorBinding
public class UiProperties {

    boolean testMode;
    boolean performanceTestMode;
    int maxUploadSizeMb;
    String supportEmail;
    String closeShortcut;
    String commitShortcut;
    String tableInsertShortcut;
    boolean createActionAddsFirst;
    String tableAddShortcut;
    String tableRemoveShortcut;
    String tableEditShortcut;
    String tableViewShortcut;
    String nextTabShortcut;
    String previousTabShortcut;
    String pickerShortcutModifiers;
    String pickerLookupShortcut;
    String pickerOpenShortcut;
    String pickerClearShortcut;
    boolean useSaveConfirmation;
    boolean layoutAnalyzerEnabled;
    boolean manualScreenSettingsSaving;
    int lookupFieldPageLength;
    String validationNotificationType;
    boolean reloadUnfetchedAttributesFromLookupScreens;
    int httpSessionExpirationTimeoutSec;
    int maxTabCount;
    boolean showBreadCrumbs;
    int mainTabCaptionLength;
    UrlHandlingMode urlHandlingMode;
    String appWindowTheme;
    String embeddedResourcesRoot;
    List<String> linkHandlerActions;
    int tablePageLength;
    double tableCacheRate;
    MainTabSheetMode mainTabSheetMode;
    ManagedMainTabSheetMode managedMainTabSheetMode;
    boolean defaultScreenCanBeClosed;
    String loginScreenId;
    String mainScreenId;
    String initialScreenId;
    boolean forceRefreshAuthenticatedTabs;
    boolean allowAnonymousAccess;
    boolean htmlSanitizerEnabled;
    String iconsConfig;
    private boolean compositeMenu;

    public UiProperties(
            boolean testMode,
            boolean performanceTestMode,
            @DefaultValue("20") int maxUploadSizeMb,
            String supportEmail,
            @DefaultValue("ESCAPE") String closeShortcut,
            @DefaultValue("CTRL-ENTER") String commitShortcut,
            @DefaultValue("CTRL-BACKSLASH") String tableInsertShortcut,
            @DefaultValue("true") boolean createActionAddsFirst,
            @DefaultValue("CTRL-ALT-BACKSLASH") String tableAddShortcut,
            @DefaultValue("CTRL-DELETE") String tableRemoveShortcut,
            @DefaultValue("ENTER") String tableEditShortcut,
            @DefaultValue("ENTER") String tableViewShortcut,
            @DefaultValue("CTRL-SHIFT-PAGE_DOWN") String nextTabShortcut,
            @DefaultValue("CTRL-SHIFT-PAGE_UP") String previousTabShortcut,
            @DefaultValue("CTRL-ALT") String pickerShortcutModifiers,
            @DefaultValue("CTRL-ALT-L") String pickerLookupShortcut,
            @DefaultValue("CTRL-ALT-O") String pickerOpenShortcut,
            @DefaultValue("CTRL-ALT-C") String pickerClearShortcut,
            @DefaultValue("true") boolean useSaveConfirmation,
            @DefaultValue("true") boolean layoutAnalyzerEnabled,
            boolean manualScreenSettingsSaving,
            @DefaultValue("10") int lookupFieldPageLength,
            @DefaultValue("TRAY") String validationNotificationType,
            @DefaultValue("true") boolean reloadUnfetchedAttributesFromLookupScreens,
            @DefaultValue("1800") int httpSessionExpirationTimeoutSec,
            @DefaultValue("20") int maxTabCount,
            @DefaultValue("true") boolean showBreadCrumbs,
            @DefaultValue("25") int mainTabCaptionLength,
            @DefaultValue("URL_ROUTES") UrlHandlingMode urlHandlingMode,
            @DefaultValue("halo") String appWindowTheme,
            String embeddedResourcesRoot,
            @DefaultValue("open,o") List<String> linkHandlerActions,
            @DefaultValue("15") int tablePageLength,
            @DefaultValue("2") double tableCacheRate,
            @DefaultValue("DEFAULT") MainTabSheetMode mainTabSheetMode,
            @DefaultValue("HIDE_TABS") ManagedMainTabSheetMode managedMainTabSheetMode,
            @DefaultValue("true") boolean defaultScreenCanBeClosed,
            @DefaultValue("login") String loginScreenId,
            @DefaultValue("main") String mainScreenId,
            String initialScreenId,
            @DefaultValue("false") boolean forceRefreshAuthenticatedTabs,
            @DefaultValue("false") boolean allowAnonymousAccess,
            @DefaultValue("true") boolean htmlSanitizerEnabled,
            @DefaultValue("io.jmix.ui.icons.CubaIcon") String iconsConfig,
            @DefaultValue("true") boolean compositeMenu

    ) {
        this.testMode = testMode;
        this.performanceTestMode = performanceTestMode;
        this.maxUploadSizeMb = maxUploadSizeMb;
        this.supportEmail = supportEmail;
        this.closeShortcut = closeShortcut;
        this.commitShortcut = commitShortcut;
        this.tableInsertShortcut = tableInsertShortcut;
        this.createActionAddsFirst = createActionAddsFirst;
        this.tableAddShortcut = tableAddShortcut;
        this.tableRemoveShortcut = tableRemoveShortcut;
        this.tableEditShortcut = tableEditShortcut;
        this.tableViewShortcut = tableViewShortcut;
        this.nextTabShortcut = nextTabShortcut;
        this.previousTabShortcut = previousTabShortcut;
        this.pickerShortcutModifiers = pickerShortcutModifiers;
        this.pickerLookupShortcut = pickerLookupShortcut;
        this.pickerOpenShortcut = pickerOpenShortcut;
        this.pickerClearShortcut = pickerClearShortcut;
        this.useSaveConfirmation = useSaveConfirmation;
        this.layoutAnalyzerEnabled = layoutAnalyzerEnabled;
        this.manualScreenSettingsSaving = manualScreenSettingsSaving;
        this.lookupFieldPageLength = lookupFieldPageLength;
        this.validationNotificationType = validationNotificationType;
        this.reloadUnfetchedAttributesFromLookupScreens = reloadUnfetchedAttributesFromLookupScreens;
        this.httpSessionExpirationTimeoutSec = httpSessionExpirationTimeoutSec;
        this.maxTabCount = maxTabCount;
        this.showBreadCrumbs = showBreadCrumbs;
        this.mainTabCaptionLength = mainTabCaptionLength;
        this.urlHandlingMode = urlHandlingMode;
        this.appWindowTheme = appWindowTheme;
        this.embeddedResourcesRoot = embeddedResourcesRoot;
        this.linkHandlerActions = linkHandlerActions;
        this.tablePageLength = tablePageLength;
        this.tableCacheRate = tableCacheRate;
        this.mainTabSheetMode = mainTabSheetMode;
        this.managedMainTabSheetMode = managedMainTabSheetMode;
        this.defaultScreenCanBeClosed = defaultScreenCanBeClosed;
        this.loginScreenId = loginScreenId;
        this.mainScreenId = mainScreenId;
        this.initialScreenId = initialScreenId;
        this.forceRefreshAuthenticatedTabs = forceRefreshAuthenticatedTabs;
        this.allowAnonymousAccess = allowAnonymousAccess;
        this.htmlSanitizerEnabled = htmlSanitizerEnabled;
        this.iconsConfig = iconsConfig;
        this.compositeMenu = compositeMenu;
    }

    public boolean isCreateActionAddsFirst() {
        return createActionAddsFirst;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public boolean isPerformanceTestMode() {
        return performanceTestMode;
    }

    public int getMaxUploadSizeMb() {
        return maxUploadSizeMb;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public String getCloseShortcut() {
        return closeShortcut;
    }

    public String getCommitShortcut() {
        return commitShortcut;
    }

    public String getTableInsertShortcut() {
        return tableInsertShortcut;
    }

    public String getTableAddShortcut() {
        return tableAddShortcut;
    }

    public String getTableRemoveShortcut() {
        return tableRemoveShortcut;
    }

    public String getTableEditShortcut() {
        return tableEditShortcut;
    }

    public String getTableViewShortcut() {
        return tableViewShortcut;
    }

    public String getNextTabShortcut() {
        return nextTabShortcut;
    }

    public String getPreviousTabShortcut() {
        return previousTabShortcut;
    }

    public String getPickerShortcutModifiers() {
        return pickerShortcutModifiers;
    }

    public String getPickerLookupShortcut() {
        return pickerLookupShortcut;
    }

    public String getPickerOpenShortcut() {
        return pickerOpenShortcut;
    }

    public String getPickerClearShortcut() {
        return pickerClearShortcut;
    }

    public boolean isUseSaveConfirmation() {
        return useSaveConfirmation;
    }

    public boolean isLayoutAnalyzerEnabled() {
        return layoutAnalyzerEnabled;
    }

    public boolean isManualScreenSettingsSaving() {
        return manualScreenSettingsSaving;
    }

    public int getLookupFieldPageLength() {
        return lookupFieldPageLength;
    }

    /**
     * Standard Window validation error notification type as one of Notifications.NotificationType values.
     */
    public String getValidationNotificationType() {
        return validationNotificationType;
    }

    /**
     * If true, then LookupBuilder and TokenList will reload entity after it is selected from lookup window if the selected entity doesn't contain all
     * required entity attributes
     */
    public boolean isReloadUnfetchedAttributesFromLookupScreens() {
        return reloadUnfetchedAttributesFromLookupScreens;
    }

    /**
     * HTTP session expiration timeout in seconds.<br>
     * Should be equal or less than user session timeout.
     */
    public int getHttpSessionExpirationTimeoutSec() {
        return httpSessionExpirationTimeoutSec;
    }

    /**
     * Maximum number of opened tabs. 0 for unlimited.
     */
    public int getMaxTabCount() {
        return maxTabCount;
    }

    public boolean isShowBreadCrumbs() {
        return showBreadCrumbs;
    }

    /**
     * Maximum number of symbols in main tabs captions.
     */
    public int getMainTabCaptionLength() {
        return mainTabCaptionLength;
    }

    public UrlHandlingMode getUrlHandlingMode() {
        return urlHandlingMode;
    }

    public String getAppWindowTheme() {
        return appWindowTheme;
    }

    public String getEmbeddedResourcesRoot() {
        return embeddedResourcesRoot;
    }

    public List<String> getLinkHandlerActions() {
        return linkHandlerActions;
    }

    /**
     * Sets the page length for Table implementation - count of rows for first rendering of Table. After first partial
     * rendering Table will request rest of rows from the server.
     * <br>
     * Setting page length 0 disables paging.
     * <br>
     * If Table has fixed height the client side may update the page length automatically the correct value.
     */
    public int getTablePageLength() {
        return tablePageLength;
    }

    /**
     * This property adjusts a possible caching mechanism of table implementation.
     * <br>
     * Table component may fetch and render some rows outside visible area. With complex tables (for example containing
     * layouts and components), the client side may become unresponsive. Setting the value lower, UI will become more
     * responsive. With higher values scrolling in client will hit server less frequently.
     * <br>
     * The amount of cached rows will be cacheRate multiplied with pageLength {@link #getTablePageLength()} both below
     * and above visible area.
     */
    public double getTableCacheRate() {
        return tableCacheRate;
    }

    /**
     * Sets whether default {@link io.jmix.ui.widgets.CubaMainTabSheet} or
     * {@link io.jmix.ui.widgets.CubaManagedTabSheet} will be used in AppWorkArea.
     */
    public MainTabSheetMode getMainTabSheetMode() {
        return mainTabSheetMode;
    }

    /**
     * Sets how the managed main TabSheet switches its tabs: hides or unloads them.
     */
    public ManagedMainTabSheetMode getManagedMainTabSheetMode() {
        return managedMainTabSheetMode;
    }

    /**
     * Defines whether default screen can be closed or not when TABBED work area mode is used.
     */
    public boolean isDefaultScreenCanBeClosed() {
        return defaultScreenCanBeClosed;
    }

    public String getLoginScreenId() {
        return loginScreenId;
    }

    public String getMainScreenId() {
        return mainScreenId;
    }

    public String getInitialScreenId() {
        return initialScreenId;
    }

    public boolean isForceRefreshAuthenticatedTabs() {
        return forceRefreshAuthenticatedTabs;
    }

    public boolean isAllowAnonymousAccess() {
        return allowAnonymousAccess;
    }

    /**
     * Defines whether to sanitize the value of components using {@link HtmlSanitizer}
     * to prevent Cross-site Scripting (XSS) in HTML context.
     */
    public boolean isHtmlSanitizerEnabled() {
        return htmlSanitizerEnabled;
    }

    public String getIconsConfig() {
        return iconsConfig;
    }

    public boolean isCompositeMenu() {
        return compositeMenu;
    }
}

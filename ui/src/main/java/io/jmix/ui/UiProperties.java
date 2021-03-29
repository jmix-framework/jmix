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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "jmix.ui")
@ConstructorBinding
public class UiProperties {

    boolean testMode;
    boolean performanceTestMode;
    boolean productionMode;

    String theme;
    String appWindowMode;
    boolean compositeMenu;
    boolean allowAnonymousAccess;
    int jmxConsoleMBeanOperationTimeoutSec;
    int httpSessionExpirationTimeoutSec;
    UrlHandlingMode urlHandlingMode;
    List<String> linkHandlerActions;
    List<String> viewFileExtensions;
    int saveExportedByteArrayDataThresholdBytes;
    long webJarResourcesCacheTime;
    long backgroundTaskTimeoutCheckInterval;

    Integer defaultMaxFetchSize;
    Map<String, Integer> entityMaxFetchSize;

    Integer defaultPageSize;
    Map<String, Integer> entityPageSize;

    int maxTabCount;
    boolean showBreadCrumbs;
    boolean defaultScreenCanBeClosed;
    String defaultScreenId;
    String loginScreenId;
    String mainScreenId;
    String initialScreenId;
    boolean forceRefreshAuthenticatedTabs;
    int mainTabCaptionLength;

    public UiProperties(
            boolean testMode,
            boolean performanceTestMode,
            @DefaultValue("true") boolean productionMode,
            @DefaultValue("helium") String theme,
            @DefaultValue("TABBED") String appWindowMode,
            @DefaultValue("true") boolean compositeMenu,
            @DefaultValue("false") boolean allowAnonymousAccess,
            @DefaultValue("600") int jmxConsoleMBeanOperationTimeoutSec,
            @DefaultValue("1800") int httpSessionExpirationTimeoutSec,
            @DefaultValue("URL_ROUTES") UrlHandlingMode urlHandlingMode,
            @DefaultValue({"open", "o"}) List<String> linkHandlerActions,
            @DefaultValue({"htm", "html", "jpg", "png", "jpeg", "pdf"}) List<String> viewFileExtensions,
            @DefaultValue("102400") int saveExportedByteArrayDataThresholdBytes,
            @DefaultValue("31536000") long webJarResourcesCacheTime, // 60 * 60 * 24 * 365
            @DefaultValue("5000") long backgroundTaskTimeoutCheckInterval,
            @DefaultValue("10000") Integer defaultMaxFetchSize,
            @Nullable Map<String, Integer> entityMaxFetchSize,
            @DefaultValue("50") Integer defaultPageSize,
            @Nullable Map<String, Integer> entityPageSize,
            @DefaultValue("20") int maxTabCount,
            @DefaultValue("true") boolean showBreadCrumbs,
            @DefaultValue("true") boolean defaultScreenCanBeClosed,
            String defaultScreenId,
            @DefaultValue("login") String loginScreenId,
            @DefaultValue("main") String mainScreenId,
            String initialScreenId,
            @DefaultValue("false") boolean forceRefreshAuthenticatedTabs,
            @DefaultValue("25") int mainTabCaptionLength
    ) {
        this.testMode = testMode;
        this.performanceTestMode = performanceTestMode;
        this.productionMode = productionMode;
        this.theme = theme;
        this.appWindowMode = appWindowMode;
        this.compositeMenu = compositeMenu;
        this.allowAnonymousAccess = allowAnonymousAccess;
        this.jmxConsoleMBeanOperationTimeoutSec = jmxConsoleMBeanOperationTimeoutSec;
        this.httpSessionExpirationTimeoutSec = httpSessionExpirationTimeoutSec;
        this.urlHandlingMode = urlHandlingMode;
        this.linkHandlerActions = linkHandlerActions;
        this.allowAnonymousAccess = allowAnonymousAccess;
        this.compositeMenu = compositeMenu;
        this.viewFileExtensions = viewFileExtensions;
        this.saveExportedByteArrayDataThresholdBytes = saveExportedByteArrayDataThresholdBytes;
        this.webJarResourcesCacheTime = webJarResourcesCacheTime;
        this.backgroundTaskTimeoutCheckInterval = backgroundTaskTimeoutCheckInterval;
        this.defaultMaxFetchSize = defaultMaxFetchSize;
        this.entityMaxFetchSize = entityMaxFetchSize == null ? Collections.emptyMap() : entityMaxFetchSize;
        this.defaultPageSize = defaultPageSize;
        this.entityPageSize = entityPageSize == null ? Collections.emptyMap() : entityPageSize;
        this.maxTabCount = maxTabCount;
        this.showBreadCrumbs = showBreadCrumbs;
        this.defaultScreenCanBeClosed = defaultScreenCanBeClosed;
        this.defaultScreenId = defaultScreenId;
        this.loginScreenId = loginScreenId;
        this.mainScreenId = mainScreenId;
        this.initialScreenId = initialScreenId;
        this.forceRefreshAuthenticatedTabs = forceRefreshAuthenticatedTabs;
        this.mainTabCaptionLength = mainTabCaptionLength;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public boolean isPerformanceTestMode() {
        return performanceTestMode;
    }

    /**
     * @return Default main window mode.
     */
    public String getAppWindowMode() {
        return appWindowMode;
    }

    /**
     * HTTP session expiration timeout in seconds.<br>
     * Should be equal or less than user session timeout.
     */
    public int getHttpSessionExpirationTimeoutSec() {
        return httpSessionExpirationTimeoutSec;
    }

    public UrlHandlingMode getUrlHandlingMode() {
        return urlHandlingMode;
    }

    public String getTheme() {
        return theme;
    }

    public List<String> getLinkHandlerActions() {
        return linkHandlerActions;
    }

    public boolean isAllowAnonymousAccess() {
        return allowAnonymousAccess;
    }

    public boolean isCompositeMenu() {
        return compositeMenu;
    }

    public List<String> getViewFileExtensions() {
        return viewFileExtensions;
    }

    public int getSaveExportedByteArrayDataThresholdBytes() {
        return saveExportedByteArrayDataThresholdBytes;
    }

    /**
     * Enables to configure whether WebJar resources should be cached or not.
     * <p>
     * Zero cache time disables caching at all.
     *
     * @return WebJar resources cache time in seconds
     */
    public long getWebJarResourcesCacheTime() {
        return webJarResourcesCacheTime;
    }

    public boolean isProductionMode() {
        return productionMode;
    }

    /**
     * @return Timeout (in seconds) for MBean operation invoked in JMX console
     */
    public int getJmxConsoleMBeanOperationTimeoutSec() {
        return jmxConsoleMBeanOperationTimeoutSec;
    }

    /**
     * Interval for checking timeout of a BackgroundTask.
     *
     * @return timeout in ms
     */
    public long getBackgroundTaskTimeoutCheckInterval() {
        return backgroundTaskTimeoutCheckInterval;
    }

    public int getDefaultMaxFetchSize() {
        return defaultMaxFetchSize;
    }

    public int getEntityMaxFetchSize(String entityName) {
        Integer forEntity = entityMaxFetchSize.get(entityName);
        if (forEntity != null)
            return forEntity;
        return defaultMaxFetchSize;
    }

    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    public int getEntityPageSize(String entityName) {
        Integer forEntity = entityPageSize.get(entityName);
        if (forEntity != null)
            return forEntity;
        return defaultPageSize;
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

    /**
     * Maximum number of symbols in main tabs captions.
     */
    public int getMainTabCaptionLength() {
        return mainTabCaptionLength;
    }
}

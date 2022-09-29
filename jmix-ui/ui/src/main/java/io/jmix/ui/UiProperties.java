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

    /**
     * Enables automatic tests for UI. If {@code true} components add {@code j-test-id} attribute to DOM tree.
     */
    boolean testMode;

    /**
     * Enables performance testing for UI:
     * <ul>
     *     <li>disables {@code xsrf-protection}</li>
     *     <li>Vaadin generates the same ids for component connectors.</li>
     * </ul>
     */
    boolean performanceTestMode;

    /**
     * Whether application is in production mode
     */
    boolean productionMode;

    /**
     * Default main window mode.
     */
    String appWindowMode;
    boolean compositeMenu;
    boolean allowAnonymousAccess;

    /**
     * Timeout (in seconds) for MBean operation invoked in JMX console
     */
    int jmxConsoleMBeanOperationTimeoutSec;

    /**
     * HTTP session expiration timeout in seconds. Should be equal or less than user session timeout.
     */
    int httpSessionExpirationTimeoutSec;
    UrlHandlingMode urlHandlingMode;
    List<String> viewFileExtensions;

    /**
     * Threshold in bytes on which downloaded through {@code ByteArrayDataProvider} byte arrays will be saved to
     * temporary files to prevent HTTP session memory leaks. Default is 100 KB.
     */
    int saveExportedByteArrayDataThresholdBytes;

    /**
     * WebJar resources cache time in seconds. Zero cache time disables caching at all.
     */
    long webJarResourcesCacheTime;

    /**
     * Interval for checking timeout of a BackgroundTask in ms.
     */
    long backgroundTaskTimeoutCheckInterval;

    Integer defaultMaxFetchSize;
    Map<String, Integer> entityMaxFetchSize;

    Integer defaultPageSize;
    Map<String, Integer> entityPageSize;

    /**
     * Maximum number of opened tabs. 0 for unlimited.
     */
    int maxTabCount;

    /**
     * Whether WindowBreadCrumbs is shown in screens.
     */
    boolean showBreadCrumbs;

    /**
     * Whether default screen can be closed or not when TABBED work area mode is used.
     */
    boolean defaultScreenCanBeClosed;

    /**
     * What screen should be opened after login. This setting will be applied to all users.
     */
    String defaultScreenId;

    /**
     * Screen that will be used as Login screen.
     */
    String loginScreenId;

    /**
     * Screen that will be used as Main screen.
     */
    String mainScreenId;

    /**
     * Screen that will be open for non-authenticated user when an application opened.
     */
    String initialScreenId;

    /**
     * Whether the locale select field is shown on login screen.
     */
    boolean localeSelectVisible;

    /**
     * Maximum number of symbols in main tabs captions.
     */
    int mainTabCaptionLength;

    public UiProperties(
            boolean testMode,
            boolean performanceTestMode,
            @DefaultValue("false") boolean productionMode,
            @DefaultValue("TABBED") String appWindowMode,
            @DefaultValue("true") boolean compositeMenu,
            @DefaultValue("false") boolean allowAnonymousAccess,
            @DefaultValue("600") int jmxConsoleMBeanOperationTimeoutSec,
            @DefaultValue("1800") int httpSessionExpirationTimeoutSec,
            @DefaultValue("URL_ROUTES") UrlHandlingMode urlHandlingMode,
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
            @DefaultValue("true") boolean localeSelectVisible,
            @DefaultValue("25") int mainTabCaptionLength
    ) {
        this.testMode = testMode;
        this.performanceTestMode = performanceTestMode;
        this.productionMode = productionMode;
        this.appWindowMode = appWindowMode;
        this.compositeMenu = compositeMenu;
        this.allowAnonymousAccess = allowAnonymousAccess;
        this.jmxConsoleMBeanOperationTimeoutSec = jmxConsoleMBeanOperationTimeoutSec;
        this.httpSessionExpirationTimeoutSec = httpSessionExpirationTimeoutSec;
        this.urlHandlingMode = urlHandlingMode;
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
        this.localeSelectVisible = localeSelectVisible;
        this.mainTabCaptionLength = mainTabCaptionLength;
    }

    /**
     * @see #testMode
     */
    public boolean isTestMode() {
        return testMode;
    }

    /**
     * @see #performanceTestMode
     */
    public boolean isPerformanceTestMode() {
        return performanceTestMode;
    }

    /**
     * @see #appWindowMode
     */
    public String getAppWindowMode() {
        return appWindowMode;
    }

    /**
     * @see #httpSessionExpirationTimeoutSec
     */
    public int getHttpSessionExpirationTimeoutSec() {
        return httpSessionExpirationTimeoutSec;
    }

    public UrlHandlingMode getUrlHandlingMode() {
        return urlHandlingMode;
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

    /**
     * @see #saveExportedByteArrayDataThresholdBytes
     */
    public int getSaveExportedByteArrayDataThresholdBytes() {
        return saveExportedByteArrayDataThresholdBytes;
    }

    /**
     * @see #webJarResourcesCacheTime
     */
    public long getWebJarResourcesCacheTime() {
        return webJarResourcesCacheTime;
    }

    /**
     * @see #productionMode
     */
    public boolean isProductionMode() {
        return productionMode;
    }

    /**
     * @see #jmxConsoleMBeanOperationTimeoutSec
     */
    public int getJmxConsoleMBeanOperationTimeoutSec() {
        return jmxConsoleMBeanOperationTimeoutSec;
    }

    /**
     * @see #backgroundTaskTimeoutCheckInterval
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
     * @see #maxTabCount
     */
    public int getMaxTabCount() {
        return maxTabCount;
    }

    /**
     * @see #showBreadCrumbs
     */
    public boolean isShowBreadCrumbs() {
        return showBreadCrumbs;
    }

    /**
     * @see #defaultScreenCanBeClosed
     */
    public boolean isDefaultScreenCanBeClosed() {
        return defaultScreenCanBeClosed;
    }

    /**
     * @see #defaultScreenId
     */
    public String getDefaultScreenId() {
        return defaultScreenId;
    }

    /**
     * @see #loginScreenId
     */
    public String getLoginScreenId() {
        return loginScreenId;
    }

    /**
     * @see #mainScreenId
     */
    public String getMainScreenId() {
        return mainScreenId;
    }

    /**
     * @see #initialScreenId
     */
    public String getInitialScreenId() {
        return initialScreenId;
    }

    /**
     * @see #mainTabCaptionLength
     */
    public int getMainTabCaptionLength() {
        return mainTabCaptionLength;
    }

    /**
     * @see #localeSelectVisible
     */
    public boolean isLocaleSelectVisible() {
        return localeSelectVisible;
    }
}

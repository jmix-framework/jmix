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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import org.springframework.lang.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "jmix.ui")
public class UiProperties {

    /**
     * View that will be used as Login view.
     */
    String loginViewId;

    /**
     * View that will be used as Main view.
     */
    String mainViewId;

    /**
     * View that should be opened after login. This setting will be applied to all users.
     */
    String defaultViewId;

    /**
     * Defines whether menu should be built with menu items from add-ons. {@code true} means using menu items from
     * add-ons, {@code false} - using only menu configuration from the application. The default value is {@code true}.
     */
    boolean compositeMenu;

    Integer defaultMaxFetchSize;
    Map<String, Integer> entityMaxFetchSize;

    Integer defaultPageSize;
    Map<String, Integer> entityPageSize;

    /**
     * File extensions that can be opened for viewing in a browser.
     */
    List<String> viewFileExtensions;

    /**
     * Threshold in bytes on which downloaded through {@code ByteArrayDownloadDataProvider} byte arrays will be saved to
     * temporary files to prevent HTTP session memory leaks. Default is 100 KB.
     */
    int saveExportedByteArrayDataThresholdBytes;

    /**
     * Whether to reinitialize a session after login to protect from session fixation attacks.
     */
    boolean useSessionFixationProtection;

    /**
     * Whether to set security context to request thread when websocket request from client side is processed.
     */
    boolean websocketRequestSecurityContextProvided;

    public UiProperties(@DefaultValue("login") String loginViewId,
                        @DefaultValue("main") String mainViewId,
                        @Nullable String defaultViewId,
                        @DefaultValue("true") boolean compositeMenu,
                        @DefaultValue("10000") Integer defaultMaxFetchSize,
                        @Nullable Map<String, Integer> entityMaxFetchSize,
                        @DefaultValue("50") Integer defaultPageSize,
                        @Nullable Map<String, Integer> entityPageSize,
                        @DefaultValue({"htm", "html", "jpg", "png", "jpeg", "pdf"}) List<String> viewFileExtensions,
                        @DefaultValue("102400") int saveExportedByteArrayDataThresholdBytes,
                        @DefaultValue("true") boolean useSessionFixationProtection,
                        @DefaultValue("true") boolean websocketRequestSecurityContextProvided
    ) {
        this.loginViewId = loginViewId;
        this.mainViewId = mainViewId;
        this.defaultViewId = defaultViewId;
        this.compositeMenu = compositeMenu;
        this.defaultMaxFetchSize = defaultMaxFetchSize;
        this.entityMaxFetchSize = entityMaxFetchSize == null ? Collections.emptyMap() : entityMaxFetchSize;
        this.defaultPageSize = defaultPageSize;
        this.entityPageSize = entityPageSize == null ? Collections.emptyMap() : entityPageSize;
        this.viewFileExtensions = viewFileExtensions;
        this.saveExportedByteArrayDataThresholdBytes = saveExportedByteArrayDataThresholdBytes;
        this.useSessionFixationProtection = useSessionFixationProtection;
        this.websocketRequestSecurityContextProvided = websocketRequestSecurityContextProvided;
    }

    /**
     * @see #loginViewId
     */
    public String getLoginViewId() {
        return loginViewId;
    }

    /**
     * @see #mainViewId
     */
    public String getMainViewId() {
        return mainViewId;
    }

    /**
     * @see #defaultViewId
     */
    @Nullable
    public String getDefaultViewId() {
        return defaultViewId;
    }

    /**
     * @see #compositeMenu
     */
    public boolean isCompositeMenu() {
        return compositeMenu;
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
     * @see #viewFileExtensions
     */
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
     * @see #useSessionFixationProtection
     */
    public boolean isUseSessionFixationProtection() {
        return useSessionFixationProtection;
    }

    /**
     * @see #websocketRequestSecurityContextProvided
     */
    public boolean isWebsocketRequestSecurityContextProvided() {
        return websocketRequestSecurityContextProvided;
    }
}

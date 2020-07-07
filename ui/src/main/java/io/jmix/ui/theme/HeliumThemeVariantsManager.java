/*
 * Copyright (c) 2008-2020 Haulmont.
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

package io.jmix.ui.theme;

import com.google.common.base.Strings;
import io.jmix.core.CoreProperties;
import io.jmix.ui.settings.UserSettingService;
import io.jmix.ui.sys.AppCookies;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component(HeliumThemeVariantsManager.NAME)
public class HeliumThemeVariantsManager {
    public static final String NAME = "helium_HeliumThemeVariantsManager";

    protected static final String THEME_SIZE_USER_SETTING_NAME = "heliumThemeSize";
    protected static final String THEME_MODE_USER_SETTING_NAME = "heliumThemeMode";

    protected static final String THEME_MODE_COOKIE_PREFIX = "HELIUM_THEME_MODE_";
    protected static final String THEME_SIZE_COOKIE_PREFIX = "HELIUM_THEME_SIZE_";

    protected static final String DEFAULT_THEME_MODE_KEY = "helium.defaultMode";
    protected static final String DEFAULT_THEME_SIZE_KEY = "helium.defaultSize";

    protected static final String THEME_MODE_LIST_KEY = "helium.modes";
    protected static final String THEME_SIZE_LIST_KEY = "helium.sizes";

    @Autowired
    protected CoreProperties coreProperties;

    @Autowired(required = false)
    protected UserSettingService userSettingService;

    @Autowired
    protected ThemeConstantsManager themeConstantsManager;

    protected AppCookies cookies;

    public HeliumThemeVariantsManager() {
        cookies = new AppCookies();
    }

    @Nullable
    public String getUserAppThemeMode() {
        return getCookieValue(THEME_MODE_COOKIE_PREFIX);
    }

    public void setUserAppThemeMode(String themeMode) {
        if (Objects.equals(themeMode, getDefaultAppThemeMode())) {
            removeCookie(THEME_MODE_COOKIE_PREFIX);
            removeUserSetting(THEME_MODE_USER_SETTING_NAME);
        } else {
            addCookie(THEME_MODE_COOKIE_PREFIX, themeMode);
            saveUserSetting(THEME_MODE_USER_SETTING_NAME, themeMode);
        }
    }

    @Nullable
    public String loadUserAppThemeModeSetting() {
        return loadUserSetting(THEME_MODE_USER_SETTING_NAME);
    }

    @Nullable
    public String loadUserAppThemeModeSettingOrDefault() {
        String mode = loadUserSetting(THEME_MODE_USER_SETTING_NAME);
        return Strings.isNullOrEmpty(mode) ? getDefaultAppThemeMode() : mode;
    }

    public String getDefaultAppThemeMode() {
        return getThemeConstant(DEFAULT_THEME_MODE_KEY);
    }

    public List<String> getAppThemeModeList() {
        return getThemeConstantAsList(THEME_MODE_LIST_KEY);
    }

    @Nullable
    public String getUserAppThemeSize() {
        return getCookieValue(THEME_SIZE_COOKIE_PREFIX);
    }

    public void setUserAppThemeSize(String themeSize) {
        if (Objects.equals(themeSize, getDefaultAppThemeSize())) {
            removeCookie(THEME_SIZE_COOKIE_PREFIX);
            removeUserSetting(THEME_SIZE_USER_SETTING_NAME);
        } else {
            addCookie(THEME_SIZE_COOKIE_PREFIX, themeSize);
            saveUserSetting(THEME_SIZE_USER_SETTING_NAME, themeSize);
        }
    }

    @Nullable
    public String loadUserAppThemeSizeSetting() {
        return loadUserSetting(THEME_SIZE_USER_SETTING_NAME);
    }

    @Nullable
    public String loadUserAppThemeSizeSettingOrDefault() {
        String size = loadUserSetting(THEME_SIZE_USER_SETTING_NAME);
        return Strings.isNullOrEmpty(size) ? getDefaultAppThemeSize() : size;
    }

    public String getDefaultAppThemeSize() {
        return getThemeConstant(DEFAULT_THEME_SIZE_KEY);
    }

    public List<String> getAppThemeSizeList() {
        return getThemeConstantAsList(THEME_SIZE_LIST_KEY);
    }

    protected String getThemeConstant(String key) {
        return themeConstantsManager.getConstants().get(key);
    }

    protected List<String> getThemeConstantAsList(String key) {
        String value = themeConstantsManager.getConstants().get(key);

        List<String> stringList = Collections.emptyList();
        if (StringUtils.isNotEmpty(value)) {
            String[] elements = value.split("\\|");
            for (String element : elements) {
                if (StringUtils.isNotEmpty(element)) {
                    if (stringList.isEmpty()) {
                        stringList = new ArrayList<>();
                    }
                    stringList.add(element);
                }
            }
        }
        return stringList;
    }

    protected void addCookie(String name, String value) {
        cookies.addCookie(getFullCookieName(name), value);
    }

    @Nullable
    protected String getCookieValue(String name) {
        return cookies.getCookieValue(getFullCookieName(name));
    }

    protected void removeCookie(String name) {
        cookies.removeCookie(getFullCookieName(name));
    }

    protected String getFullCookieName(String prefix) {
        String contextName = coreProperties.getWebContextName();
        return prefix + (Strings.isNullOrEmpty(contextName) ? "ROOT" : contextName);
    }

    protected void saveUserSetting(String name, String value) {
        if (userSettingService != null)
            userSettingService.saveSetting(name, value);
    }

    @Nullable
    protected String loadUserSetting(String name) {
        return userSettingService == null ? null : userSettingService.loadSetting(name);
    }

    protected void removeUserSetting(String name) {
        if (userSettingService != null)
            userSettingService.deleteSettings(name);
    }
}

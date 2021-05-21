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
import io.jmix.ui.UiThemeProperties;
import io.jmix.ui.settings.UserSettingService;
import io.jmix.ui.sys.AppCookies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import java.util.List;

@Component("ui_ThemeVariantsManager")
public class ThemeVariantsManager {

    protected static final String THEME_SIZE_USER_SETTING_NAME = "themeSize";
    protected static final String THEME_MODE_USER_SETTING_NAME = "themeMode";

    protected static final String THEME_MODE_COOKIE_PREFIX = "UI_THEME_MODE_";
    protected static final String THEME_SIZE_COOKIE_PREFIX = "UI_THEME_SIZE_";

    @Autowired(required = false)
    protected ServletContext servletContext;

    @Autowired(required = false)
    protected UserSettingService userSettingService;

    @Autowired
    protected UiThemeProperties themeProperties;

    protected AppCookies cookies;

    public ThemeVariantsManager() {
        cookies = new AppCookies();
    }

    @Nullable
    public String getThemeModeCookieValue() {
        return getCookieValue(THEME_MODE_COOKIE_PREFIX);
    }

    public void setThemeMode(@Nullable String themeMode) {
        if (!Strings.isNullOrEmpty(themeMode)) {
            addCookie(THEME_MODE_COOKIE_PREFIX, themeMode);
            saveUserSetting(THEME_MODE_USER_SETTING_NAME, themeMode);
        } else {
            removeCookie(THEME_MODE_COOKIE_PREFIX);
            removeUserSetting(THEME_MODE_USER_SETTING_NAME);
        }
    }

    @Nullable
    public String getThemeModeUserSetting() {
        return loadUserSetting(THEME_MODE_USER_SETTING_NAME);
    }

    public String getThemeModeUserSettingOrDefault() {
        String mode = getThemeModeUserSetting();
        return Strings.isNullOrEmpty(mode) ? getDefaultThemeModeToUse() : mode;
    }

    public String getDefaultThemeMode() {
        return themeProperties.getDefaultMode();
    }

    public String getDefaultThemeModeToUse() {
        String defaultModeToUse = themeProperties.getDefaultModeToUse();
        return Strings.isNullOrEmpty(defaultModeToUse) ? getDefaultThemeMode() : defaultModeToUse;
    }

    public List<String> getThemeModeList() {
        return themeProperties.getModes();
    }

    @Nullable
    public String getThemeSizeCookieValue() {
        return getCookieValue(THEME_SIZE_COOKIE_PREFIX);
    }

    public void setThemeSize(@Nullable String themeSize) {
        if (!Strings.isNullOrEmpty(themeSize)) {
            addCookie(THEME_SIZE_COOKIE_PREFIX, themeSize);
            saveUserSetting(THEME_SIZE_USER_SETTING_NAME, themeSize);
        } else {
            removeCookie(THEME_SIZE_COOKIE_PREFIX);
            removeUserSetting(THEME_SIZE_USER_SETTING_NAME);
        }
    }

    @Nullable
    public String getThemeSizeUserSetting() {
        return loadUserSetting(THEME_SIZE_USER_SETTING_NAME);
    }

    public String getThemeSizeUserSettingOrDefault() {
        String size = getThemeSizeUserSetting();
        return Strings.isNullOrEmpty(size) ? getDefaultThemeSizeToUse() : size;
    }

    public String getDefaultThemeSize() {
        return themeProperties.getDefaultSize();
    }

    public String getDefaultThemeSizeToUse() {
        String defaultSizeToUse = themeProperties.getDefaultSizeToUse();
        return Strings.isNullOrEmpty(defaultSizeToUse) ? getDefaultThemeSize() : defaultSizeToUse;
    }

    public List<String> getThemeSizeList() {
        return themeProperties.getSizes();
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
        String contextPath = servletContext == null ? null : servletContext.getContextPath();
        return prefix + (Strings.isNullOrEmpty(contextPath) ? "ROOT" : contextPath.substring(1));
    }

    protected void saveUserSetting(String name, String value) {
        if (userSettingService != null) {
            userSettingService.saveSetting(name, value);
        }
    }

    @Nullable
    protected String loadUserSetting(String name) {
        return userSettingService == null ? null : userSettingService.loadSetting(name);
    }

    protected void removeUserSetting(String name) {
        if (userSettingService != null) {
            userSettingService.deleteSettings(name);
        }
    }
}

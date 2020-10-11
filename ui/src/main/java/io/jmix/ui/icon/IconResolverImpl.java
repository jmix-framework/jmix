/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.icon;

import com.google.common.collect.ImmutableList;
import com.vaadin.server.Resource;
import io.jmix.ui.App;
import io.jmix.ui.theme.ThemeConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.List;

@Component("ui_IconResolver")
public class IconResolverImpl implements IconResolver {
    protected static final String THEME_PREFIX = "theme://";
    protected static final List<String> PROPERTY_PREFIXES = ImmutableList.of("icons.", "cuba.web.");

    private static final Logger log = LoggerFactory.getLogger(IconResolverImpl.class);

    @Autowired
    protected Icons icons;
    @Autowired
    protected List<IconProvider> iconProviders;

    @Nullable
    @Override
    public Resource getIconResource(@Nullable String iconPath) {
        if (StringUtils.isEmpty(iconPath)) {
            return null;
        }

        String themeIcon = getThemeIcon(processPath(iconPath));
        if (StringUtils.isNotEmpty(themeIcon)) {
            return getResource(themeIcon);
        }

        return getResource(iconPath);
    }

    @Nullable
    protected Resource getResource(String iconPath) {
        return iconProviders.stream()
                .filter(p -> p.canProvide(iconPath))
                .findFirst()
                .map(p -> p.getIconResource(iconPath))
                .orElseGet(() -> {
                    log.warn("There is no IconProvider for the given icon: {}", iconPath);
                    return null;
                });
    }

    @Nullable
    protected String getThemeIcon(String iconName) {
        ThemeConstants theme = App.getInstance().getThemeConstants();

        String themeIcon = null;
        for (String prefix : PROPERTY_PREFIXES) {
            themeIcon = theme.get(prefix + iconName);

            if (StringUtils.isNotEmpty(themeIcon)) {
                break;
            }
        }

        return themeIcon;
    }

    protected String processPath(String iconPath) {
        if (iconPath.startsWith(THEME_PREFIX)) {
            iconPath = iconPath.substring(THEME_PREFIX.length());
        }

        return StringUtils.replaceChars(iconPath, "/:", "..");
    }
}

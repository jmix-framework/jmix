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

import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.jmix.core.JmixModules;
import io.jmix.core.JmixOrder;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.theme.ThemeConstantsManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Component("ui_Icons")
public class IconsImpl implements Icons {

    private static final Logger log = LoggerFactory.getLogger(IconsImpl.class);

    @Autowired
    protected ThemeConstantsManager themeConstantsManager;

    @Autowired
    protected JmixModules modules;

    protected LoadingCache<String, String> iconsCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(@Nonnull String key) {
                    return resolveIcon(key);
                }
            });

    protected List<Class<? extends Icon>> iconSets = new ArrayList<>();

    @EventListener(ContextRefreshedEvent.class)
    @Order(JmixOrder.HIGHEST_PRECEDENCE + 100)
    public void init() {
        String iconSetsProp = String.join(" ", modules.getPropertyValues("jmix.ui.icons-config"));

        if (StringUtils.isEmpty(iconSetsProp)) {
            return;
        }

        Iterable<String> iconSetsClasses = Splitter.on(' ')
                .omitEmptyStrings()
                .trimResults()
                .split(iconSetsProp);

        for (String iconSetFqn : iconSetsClasses) {
            Class<?> iconSetClass;
            try {
                iconSetClass = ReflectionHelper.loadClass(iconSetFqn);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Unable to load icon set class: " + iconSetFqn, e);
            }

            if (!Icon.class.isAssignableFrom(iconSetClass)) {
                log.warn("Class {} does not implement Icon", iconSetClass);
                continue;
            }

            //noinspection unchecked
            iconSets.add((Class<? extends Icon>) iconSetClass);
        }
    }

    @Nullable
    @Override
    public String get(@Nullable Icon icon) {
        if (icon == null)
            return null;

        return get(icon.iconName());
    }

    @Nullable
    @Override
    public String get(@Nullable String icon) {
        if (StringUtils.isEmpty(icon))
            return null;

        if (!ICON_NAME_REGEX.matcher(icon).matches())
            throw new IllegalArgumentException("Icon name can contain only uppercase letters and underscores.");

        String themeIcon = getThemeIcon(icon);

        if (StringUtils.isNotEmpty(themeIcon))
            return themeIcon;

        return iconsCache.getUnchecked(icon);
    }

    protected String getThemeIcon(String iconName) {
        ThemeConstants theme = themeConstantsManager.getConstants();

        String icon = iconName.replace("/", ".");

        String themeIcon = theme.get("icons." + icon);

        if (StringUtils.isEmpty(themeIcon)) {
            themeIcon = theme.get("cuba.web." + icon);
        }

        return themeIcon;
    }

    protected String resolveIcon(String iconName) {
        String iconSource = null;

        for (Class<? extends Icon> iconSet : iconSets) {
            try {
                Object obj = iconSet.getDeclaredField(iconName).get(null);
                iconSource = ((Icon) obj).source();
            } catch (IllegalAccessException | NoSuchFieldException ignored) {
                // must be ignored, because some icon sets in the sequence may not contain the icon, e.g.:
                // assuming icon sets JmixIcon > MyCompIcon > MyAppIcon,
                // JmixIcon.OK - defined, MyCompIcon.OK - overrides, MyAppIcon.OK - not defined
                // then using MyCompIcon.OK
            }
        }

        return iconSource;
    }
}
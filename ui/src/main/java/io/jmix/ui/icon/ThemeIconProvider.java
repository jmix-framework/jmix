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

import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import io.jmix.core.JmixOrder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

import static io.jmix.core.common.util.Preconditions.checkNotEmptyString;

@Component("ui_ThemeIconProvider")
@Order(JmixOrder.LOWEST_PRECEDENCE - 20)
public class ThemeIconProvider implements IconProvider {

    protected static final String THEME_PREFIX = "theme:";

    @Override
    public Resource getIconResource(String iconPath) {
        checkNotEmptyString(iconPath, "Icon path should not be empty");

        String icon = iconPath.substring(THEME_PREFIX.length());
        return new ThemeResource(icon);
    }

    @Override
    public boolean canProvide(@Nullable String iconPath) {
        return iconPath != null && !iconPath.isEmpty() && iconPath.startsWith(THEME_PREFIX);
    }
}

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

import com.vaadin.server.ClassResource;
import com.vaadin.server.Resource;
import io.jmix.core.JmixOrder;
import io.jmix.core.common.util.Preconditions;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component("ui_ClassPathIconProvider")
@Order(JmixOrder.LOWEST_PRECEDENCE - 50)
public class ClassPathIconProvider implements IconProvider {

    protected static final String CLASSPATH_PREFIX = "classpath:";

    @Override
    public Resource getIconResource(String iconPath) {
        Preconditions.checkNotEmptyString(iconPath, "Icon path should not be empty");

        String icon = iconPath.substring(CLASSPATH_PREFIX.length());
        return new ClassResource(icon);
    }

    @Override
    public boolean canProvide(@Nullable String iconPath) {
        return iconPath != null && !iconPath.isEmpty() && iconPath.startsWith(CLASSPATH_PREFIX);
    }
}
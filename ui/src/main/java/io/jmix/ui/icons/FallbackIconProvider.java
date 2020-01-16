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

package io.jmix.ui.icons;

import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import io.jmix.core.commons.util.Preconditions;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static io.jmix.ui.icons.IconProvider.LOWEST_PLATFORM_PRECEDENCE;

@Component
@Order(LOWEST_PLATFORM_PRECEDENCE - 10)
public class FallbackIconProvider implements IconProvider {

    @Override
    public Resource getIconResource(String iconPath) {
        Preconditions.checkNotEmptyString(iconPath, "Icon path should not be empty");

        return new ThemeResource(iconPath);
    }

    @Override
    public boolean canProvide(String iconPath) {
        return iconPath != null && !iconPath.isEmpty();
    }
}
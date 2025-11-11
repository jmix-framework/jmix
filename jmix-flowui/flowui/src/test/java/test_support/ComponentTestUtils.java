/*
 * Copyright 2025 Haulmont.
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

package test_support;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.FontIcon;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.common.util.Preconditions;

public final class ComponentTestUtils {

    private ComponentTestUtils() {
    }

    public static boolean isSameIcon(Component componentIcon, VaadinIcon targetIcon) {
        return isSameIcon(componentIcon, targetIcon.create());
    }

    public static boolean isSameIcon(Component componentIcon, Icon targetIcon) {
        Preconditions.checkNotNullArgument(componentIcon);
        Preconditions.checkNotNullArgument(targetIcon);

        return componentIcon.getElement().getAttribute("icon")
                .equals(targetIcon.getIcon());
    }

    public static boolean isSameFontIcon(FontIcon fontIcon, String fontFamily, String charCode) {
        Preconditions.checkNotNullArgument(fontIcon);
        Preconditions.checkNotNullArgument(fontFamily);
        Preconditions.checkNotNullArgument(charCode);

        return fontIcon.getFontFamily().equals(fontFamily)
                && fontIcon.getCharCode().equals(charCode);
    }
}

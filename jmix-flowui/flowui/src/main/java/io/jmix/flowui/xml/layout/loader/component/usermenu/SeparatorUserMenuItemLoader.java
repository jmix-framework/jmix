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

package io.jmix.flowui.xml.layout.loader.component.usermenu;

import com.google.common.base.Strings;
import io.jmix.flowui.kit.component.usermenu.HasMenuItems;
import io.jmix.flowui.xml.layout.ComponentLoader;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

@Component("flowui_SeparatorUserMenuItemLoader")
public class SeparatorUserMenuItemLoader implements UserMenuItemLoader {

    public static final String NAME = "separator";

    @Override
    public boolean supports(String itemName) {
        return NAME.equals(itemName);
    }

    @Override
    public void loadItem(Element element, HasMenuItems menu, ComponentLoader.Context context) {
        String itemId = element.attributeValue("itemId");
        if (Strings.isNullOrEmpty(itemId)) {
            menu.addSeparator();
        } else {
            menu.addSeparator(itemId);
        }
    }
}

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

package io.jmix.flowui.xml.layout.loader.component;

import com.google.common.base.Strings;
import io.jmix.flowui.component.menu.JmixListMenu;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import org.dom4j.Element;

import java.util.Optional;

public class ListMenuLoader extends AbstractComponentLoader<JmixListMenu> {

    @Override
    public void loadComponent() {
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadClassName(resultComponent, element);

        if (isLoadLoadMenuConfig(element)) {
            resultComponent.loadMenuConfig();
        }
    }

    @Override
    protected JmixListMenu createComponent() {
        return factory.create(JmixListMenu.class);
    }

    protected boolean isLoadLoadMenuConfig(Element element) {
        String loadMenuConfig = Strings.emptyToNull(element.attributeValue("loadMenuConfig"));
        return Strings.isNullOrEmpty(loadMenuConfig)
                || Boolean.parseBoolean(loadMenuConfig);
    }
}

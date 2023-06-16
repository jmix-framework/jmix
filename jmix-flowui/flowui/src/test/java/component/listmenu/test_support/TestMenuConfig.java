/*
 * Copyright 2020 Haulmont.
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

package component.listmenu.test_support;

import io.jmix.core.*;
import io.jmix.core.common.util.Dom4j;
import io.jmix.core.common.xmlparsing.Dom4jTools;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.menu.MenuConfig;
import io.jmix.flowui.menu.MenuItem;
import org.springframework.core.env.Environment;

import java.util.List;

public class TestMenuConfig extends MenuConfig {

    public TestMenuConfig(Resources resources, Messages messages, MessageTools messageTools, Dom4jTools dom4JTools,
                          Environment environment, UiProperties uiProperties, JmixModules modules,
                          Metadata metadata, MetadataTools metadataTools) {
        super(resources, messages, messageTools, dom4JTools, environment, uiProperties, modules, metadata,
                metadataTools);
    }

    public void loadTestMenu(String menuConfig) {
        rootItems.clear();
        loadMenuItems(Dom4j.readDocument(menuConfig).getRootElement(), null);
        initialized = true;
    }

    @Override
    public List<MenuItem> getRootItems() {
        return rootItems;
    }
}

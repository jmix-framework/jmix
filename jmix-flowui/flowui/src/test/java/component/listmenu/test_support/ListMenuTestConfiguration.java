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

package component.listmenu.test_support;

import io.jmix.core.*;
import io.jmix.core.common.xmlparsing.Dom4jTools;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.FlowuiProperties;
import io.jmix.flowui.menu.MenuConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

@Import({FlowuiConfiguration.class, CoreConfiguration.class})
@Configuration
public class ListMenuTestConfiguration {

    @Bean
    public MenuConfig menuConfig(Resources resources, Messages messages, MessageTools messageTools, Dom4jTools dom4JTools,
                                 Environment environment, FlowuiProperties flowuiProperties, JmixModules modules,
                                 Metadata metadata, MetadataTools metadataTools) {
        return new TestMenuConfig(resources, messages, messageTools, dom4JTools, environment, flowuiProperties,
                modules, metadata, metadataTools);
    }
}

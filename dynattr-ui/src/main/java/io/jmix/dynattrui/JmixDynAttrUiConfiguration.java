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

package io.jmix.dynattrui;

import io.jmix.core.JmixCoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.dynattr.JmixDynAttrConfiguration;
import io.jmix.ui.JmixUiConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.support.StandardScriptEvaluator;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = {JmixCoreConfiguration.class, JmixDynAttrConfiguration.class, JmixUiConfiguration.class})
public class JmixDynAttrUiConfiguration {

    @Bean
    public ScriptEvaluator scriptEvaluator() {
        StandardScriptEvaluator scriptEvaluator = new StandardScriptEvaluator();
        scriptEvaluator.setEngineName("groovy");
        return scriptEvaluator;
    }
}

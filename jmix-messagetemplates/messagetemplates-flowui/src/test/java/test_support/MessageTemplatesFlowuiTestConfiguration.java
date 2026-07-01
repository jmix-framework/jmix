/*
 * Copyright 2024 Haulmont.
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

import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.messagetemplates.MessageTemplatesConfiguration;
import io.jmix.messagetemplates.MessageTemplateProperties;
import io.jmix.messagetemplatesflowui.MessageTemplatesFlowuiConfiguration;
import io.jmix.testsupport.config.CommonCoreTestConfiguration;
import io.jmix.testsupport.config.CoreSecurityTestConfiguration;
import io.jmix.flowui.testassist.FlowuiServletTestBeans;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Locale;

@Configuration
@Import({FlowuiConfiguration.class, CoreConfiguration.class, MessageTemplatesConfiguration.class,
        MessageTemplatesFlowuiConfiguration.class,
        CommonCoreTestConfiguration.class, FlowuiServletTestBeans.class, CoreSecurityTestConfiguration.class})
@JmixModule
public class MessageTemplatesFlowuiTestConfiguration {

    @Bean
    freemarker.template.Configuration configuration(MessageTemplateProperties messageTemplateProperties) {
        freemarker.template.Configuration configuration =
                new freemarker.template.Configuration(messageTemplateProperties.getFreemarkerVersion());
        configuration.setDefaultEncoding("UTF-8");
        configuration.setLocale(Locale.US);
        return configuration;
    }
}

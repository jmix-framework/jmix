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

package io.jmix.simplesecurityflowui;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.flowui.login.LoginViewSupport;
import io.jmix.flowui.sys.ActionsConfiguration;
import io.jmix.simplesecurity.SimpleSecurityConfiguration;
import io.jmix.simplesecurityflowui.authentication.LoginViewSupportImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@ComponentScan
@Configuration
@JmixModule(dependsOn = {CoreConfiguration.class, SimpleSecurityConfiguration.class})
public class SimpleSecurityFlowuiConfiguration {

    @Bean("simsec_LoginViewSupport")
    public LoginViewSupport loginViewSupport() {
        return new LoginViewSupportImpl();
    }

    @Bean("simsec_ActionsConfiguration")
    public ActionsConfiguration actions(ApplicationContext applicationContext,
                                        AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        ActionsConfiguration actionsConfiguration = new ActionsConfiguration(applicationContext, metadataReaderFactory);
        actionsConfiguration.setBasePackages(Collections.singletonList("io.jmix.simplesecurityflowui.action"));
        return actionsConfiguration;
    }
}

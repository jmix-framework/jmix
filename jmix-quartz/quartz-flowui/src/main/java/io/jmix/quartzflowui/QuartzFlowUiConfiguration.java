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

package io.jmix.quartzflowui;

import com.vaadin.flow.component.page.Push;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.sys.ViewControllersConfiguration;
import io.jmix.quartz.QuartzConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Collections;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = {QuartzConfiguration.class, FlowuiConfiguration.class})
@PropertySource(name = "io.jmix.quartzflowui", value = "classpath:/io/jmix/quartzflowui/module.properties")
public class QuartzFlowUiConfiguration {

    @Bean("flowui_quartz_QuartzViewControllers")
    public ViewControllersConfiguration screens(ApplicationContext applicationContext,
                                                AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        ViewControllersConfiguration viewControllers
                = new ViewControllersConfiguration(applicationContext, metadataReaderFactory);
        viewControllers.setBasePackages(Collections.singletonList("io.jmix.quartzflowui"));
        return viewControllers;
    }
}

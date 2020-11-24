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

package io.jmix.autoconfigure.ui;

import io.jmix.ui.app.jmxconsole.JmxConsoleMBeanExporter;
import org.springframework.boot.autoconfigure.jmx.ParentAwareNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;
import org.springframework.jmx.export.naming.ObjectNamingStrategy;
import org.springframework.util.StringUtils;

@Configuration
public class JmxConsoleAutoConfiguration {
    private final Environment environment;

    public JmxConsoleAutoConfiguration(Environment environment) {
        this.environment = environment;
    }

//    @Bean
//    public AnnotationMBeanExporter mbeanExporter(ObjectNamingStrategy namingStrategy) {
//        AnnotationMBeanExporter exporter = new JmxConsoleMBeanExporter();
//        exporter.setNamingStrategy(namingStrategy);
//        return exporter;
//    }
//
//
//    @Bean
//    public ParentAwareNamingStrategy objectNamingStrategy() {
//        ParentAwareNamingStrategy namingStrategy = new ParentAwareNamingStrategy(new AnnotationJmxAttributeSource());
//        String defaultDomain = this.environment.getProperty("spring.jmx.default-domain");
//        if (StringUtils.hasLength(defaultDomain)) {
//            namingStrategy.setDefaultDomain(defaultDomain);
//        }
//        boolean uniqueNames = this.environment.getProperty("spring.jmx.unique-names", Boolean.class, false);
//        namingStrategy.setEnsureUniqueRuntimeObjectNames(uniqueNames);
//        return namingStrategy;
//    }

}

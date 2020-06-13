/*
 * Copyright 2019 Haulmont.
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

package io.jmix.rest;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.security.JmixCoreSecurityConfiguration;
import io.jmix.data.DataConfiguration;
import io.jmix.rest.property.RestProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@Configuration
@ComponentScan("io.jmix.rest")
@ConfigurationPropertiesScan
@JmixModule(dependsOn = {CoreConfiguration.class, DataConfiguration.class, JmixCoreSecurityConfiguration.class})
@PropertySource("classpath:/io/jmix/rest/module.properties")
public class RestConfiguration {

    @Autowired
    protected RestProperties restProperties;

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setMaxUploadSize(restProperties.getMaxUploadSize());
        return resolver;
    }
}

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

package com.sample.app;


import io.jmix.core.annotation.JmixModule;
import io.jmix.data.JmixDataConfiguration;
import io.jmix.data.persistence.JpqlSortExpressionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@Configuration
@ComponentScan
@JmixModule(dependsOn = JmixDataConfiguration.class)
public class TestAppConfiguration {

    @Autowired
    Environment environment;

    @EventListener
    private void initDatabase(ContextRefreshedEvent event) {

    }

    @Bean(JpqlSortExpressionProvider.NAME)
    protected JpqlSortExpressionProvider jpqlSortExpressionProvider() {
        return new TestJpqlSortExpressionProvider();
    }
}

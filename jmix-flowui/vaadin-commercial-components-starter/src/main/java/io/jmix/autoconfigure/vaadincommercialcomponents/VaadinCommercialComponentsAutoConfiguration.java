/*
 * Copyright 2025 Haulmont.
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

package io.jmix.autoconfigure.vaadincommercialcomponents;

import io.jmix.core.CoreConfiguration;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.sys.autowire.ReflectionCacheManager;
import io.jmix.vaadincommercialcomponents.VaadinCommercialComponentsConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;

@AutoConfiguration
@Import({CoreConfiguration.class, FlowuiConfiguration.class, VaadinCommercialComponentsConfiguration.class})
public class VaadinCommercialComponentsAutoConfiguration {

    @Bean
    public static BeanPostProcessor myServiceModifier() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof ReflectionCacheManager reflectionCacheManager) {
                    reflectionCacheManager.addSupplyMethodNames(
                            List.of("CellValueHandler", "CellDeletionHandler",
                                    "HyperlinkCellClickHandler", "SpreadsheetComponentFactory")
                    );
                }

                return bean;
            }
        };
    }
}

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

package test_support;

import io.jmix.core.JmixOrder;
import io.jmix.restds.util.RemoteServiceConfigurationCustomizer;
import io.jmix.samples.restds.common.service.ProductService;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Order(JmixOrder.HIGHEST_PRECEDENCE - 10)
public class TestRemoteServiceConfigurationCustomizer implements RemoteServiceConfigurationCustomizer {

    @Override
    public Optional<TypeFilter> getScannerIncludeFilter() {
        return Optional.of((metadataReader, metadataReaderFactory) -> {
            String className = metadataReader.getClassMetadata().getClassName();
            return ProductService.class.getName().equals(className);
        });
    }

    @Override
    public Optional<String> getStoreName(Class<?> serviceInterface) {
        return serviceInterface.equals(ProductService.class) ?
                Optional.of("restService1") : Optional.empty();
    }

    @Override
    public Optional<String> getServiceName(Class<?> serviceInterface) {
        return serviceInterface.equals(ProductService.class) ?
                Optional.of("app_Products") : Optional.empty();
    }
}

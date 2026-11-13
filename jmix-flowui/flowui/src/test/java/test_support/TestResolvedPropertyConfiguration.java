/*
 * Copyright 2026 Haulmont.
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

import io.jmix.core.MetaPropertyPathResolver;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.impl.DatatypeRange;
import io.jmix.core.metamodel.model.impl.MetaPropertyImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers a {@link MetaPropertyPathResolver} that resolves {@link TestResolvedPropertyState#PROPERTY} for
 * every entity, the way the dynamic attributes add-on resolves its {@code +code} properties. The values
 * are kept by {@link TestResolvedPropertyState}.
 */
@Configuration
public class TestResolvedPropertyConfiguration {

    @Bean
    MetaPropertyPathResolver test_ResolvedPropertyPathResolver(DatatypeRegistry datatypeRegistry) {
        return (metaClass, propertyPath) -> {
            if (!TestResolvedPropertyState.PROPERTY.equals(propertyPath)) {
                return null;
            }
            MetaPropertyImpl property = new MetaPropertyImpl(metaClass, propertyPath, false);
            property.setRange(new DatatypeRange(datatypeRegistry.get(String.class)));
            property.setType(MetaProperty.Type.DATATYPE);
            property.setJavaType(String.class);
            return new MetaPropertyPath(metaClass, property);
        };
    }
}

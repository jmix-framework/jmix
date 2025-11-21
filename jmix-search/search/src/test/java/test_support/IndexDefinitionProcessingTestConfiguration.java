/*
 * Copyright 2021 Haulmont.
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

import io.jmix.core.annotation.JmixModule;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.search.index.impl.dynattr.DynamicAttributesSupportDelegate;
import io.jmix.search.index.mapping.processor.impl.FieldMappingCreator;
import io.jmix.search.index.mapping.processor.impl.dynattr.DynamicAttributesGroupConfigurationValidator;
import io.jmix.search.index.mapping.processor.impl.dynattr.DynamicAttributesGroupProcessor;
import io.jmix.search.index.mapping.processor.impl.dynattr.DynamicAttributesResolver;
import io.jmix.search.index.mapping.processor.impl.dynattr.PatternsMatcher;
import io.jmix.search.utils.PropertyTools;
import org.springframework.context.annotation.*;

import java.util.List;

import static org.mockito.Mockito.*;

@Configuration
@JmixModule
@Import({BaseSearchTestConfiguration.class})
public class IndexDefinitionProcessingTestConfiguration {

    @Bean
    @Primary
    public DynAttrMetadata dynAttrMetadata() {
        return mock(DynAttrMetadata.class);
    }

    @Bean(name = "search_DynamicAttributesGroupProcessor")
    public DynamicAttributesGroupProcessor dynamicAttributesGroupProcessor(PropertyTools propertyTools,
                                                                           DynamicAttributesResolver dynamicAttributesResolver,
                                                                           FieldMappingCreator fieldMappingCreator,
                                                                           DynamicAttributesGroupConfigurationValidator groupChecker) {
        return new DynamicAttributesGroupProcessor(propertyTools, dynamicAttributesResolver, fieldMappingCreator, groupChecker);
    }

    @Bean(name = "search_DynamicAttributesResolver")
    public DynamicAttributesResolver dynamicAttributesResolver(DynAttrMetadata dynAttrMetadata,
                                                               PropertyTools propertyTools,
                                                               PatternsMatcher patternsMatcher){
        return new DynamicAttributesResolver(dynAttrMetadata, propertyTools, patternsMatcher);
    }

    @Bean(name = "search_DynamicAttributesSupportProxy")
    public DynamicAttributesSupportDelegate dynamicAttributesSupport(){
        return new DynamicAttributesSupportDelegate();
    }
}

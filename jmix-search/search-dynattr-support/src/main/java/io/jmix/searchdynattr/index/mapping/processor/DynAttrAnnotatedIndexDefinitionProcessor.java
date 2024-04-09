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

package io.jmix.searchdynattr.index.mapping.processor;

import io.jmix.core.InstanceNameProvider;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.Stores;
import io.jmix.core.impl.method.ContextArgumentResolverComposite;
import io.jmix.core.metamodel.datatype.impl.StringDatatype;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.impl.DatatypeRange;
import io.jmix.core.metamodel.model.impl.MetaPropertyImpl;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.IndexSettingsConfigurer;
import io.jmix.search.index.mapping.analysis.impl.IndexAnalysisElementsRegistry;
import io.jmix.search.index.mapping.processor.MappingFieldAnnotationProcessorsRegistry;
import io.jmix.search.index.mapping.processor.impl.AnnotatedIndexDefinitionProcessor;
import io.jmix.search.index.mapping.propertyvalue.PropertyValueExtractorProvider;
import io.jmix.search.index.mapping.strategy.FieldMappingStrategyProvider;
import io.jmix.search.utils.PropertyTools;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.jmix.searchdynattr.SearchDynAttrSupportConstants.DYN_ATTR_PREFIX;

@Primary
@Component("search_dynattr_support_DynAttrAnnotatedIndexDefinitionProcessor")
public class DynAttrAnnotatedIndexDefinitionProcessor extends AnnotatedIndexDefinitionProcessor {
    public DynAttrAnnotatedIndexDefinitionProcessor(Metadata metadata,
                                                    Stores stores,
                                                    MetadataTools metadataTools,
                                                    MappingFieldAnnotationProcessorsRegistry mappingFieldAnnotationProcessorsRegistry,
                                                    PropertyTools propertyTools,
                                                    FieldMappingStrategyProvider fieldMappingStrategyProvider,
                                                    InstanceNameProvider instanceNameProvider,
                                                    PropertyValueExtractorProvider propertyValueExtractorProvider,
                                                    SearchProperties searchProperties,
                                                    List<IndexSettingsConfigurer> indexSettingsConfigurers,
                                                    ContextArgumentResolverComposite resolvers,
                                                    IndexAnalysisElementsRegistry indexAnalysisElementsRegistry) {
        super(metadata, stores, metadataTools, mappingFieldAnnotationProcessorsRegistry, propertyTools, fieldMappingStrategyProvider, instanceNameProvider, propertyValueExtractorProvider, searchProperties, indexSettingsConfigurers, resolvers, indexAnalysisElementsRegistry);
    }

    @Override
    protected Map<String, MetaPropertyPath> resolveEffectiveProperties(MetaClass rootEntityMetaClass, String[] includes, String[] excludes) {
        Map<String, MetaPropertyPath> effectiveProperties = new HashMap<>();
        Arrays.stream(includes)
                .filter(StringUtils::isNotBlank)
                .forEach(included -> {
                    if (isDynAttrField(included)) {
                        MetaPropertyImpl prop = new MetaPropertyImpl(rootEntityMetaClass, included);
                        prop.setRange(new DatatypeRange(new StringDatatype()));
                        prop.setJavaType(String.class);
                        prop.setStore(stores.get(Stores.NOOP));
                        prop.setType(MetaProperty.Type.DATATYPE);
                        MetaPropertyPath metaPropertyPath = new MetaPropertyPath(rootEntityMetaClass, prop);

                        effectiveProperties.put(included, metaPropertyPath);
                        return;
                    }
                    Map<String, MetaPropertyPath> propertyPaths = propertyTools.findPropertiesByPath(rootEntityMetaClass, included);
                    Map<String, MetaPropertyPath> expandedPropertyPaths = expandEmbeddedProperties(rootEntityMetaClass, propertyPaths);
                    effectiveProperties.putAll(expandedPropertyPaths);
                });
        Arrays.stream(excludes)
                .filter(StringUtils::isNotBlank)
                .flatMap(excluded -> {
                    Map<String, MetaPropertyPath> propertyPaths = propertyTools.findPropertiesByPath(rootEntityMetaClass, excluded);
                    Map<String, MetaPropertyPath> expandedPropertyPaths = expandEmbeddedProperties(rootEntityMetaClass, propertyPaths);
                    return expandedPropertyPaths.keySet().stream();
                })
                .forEach(effectiveProperties::remove);

        return effectiveProperties;
    }

    private boolean isDynAttrField(String included) {
        return included.startsWith(DYN_ATTR_PREFIX);
    }


}

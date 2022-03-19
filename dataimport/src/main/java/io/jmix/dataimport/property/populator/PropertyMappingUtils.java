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

package io.jmix.dataimport.property.populator;

import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dataimport.configuration.ImportConfiguration;
import io.jmix.dataimport.configuration.mapping.*;
import io.jmix.dataimport.extractor.data.RawValuesSource;
import io.jmix.dataimport.property.populator.impl.CustomValueProvider;
import io.jmix.dataimport.property.populator.impl.SimplePropertyValueProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("datimp_PropertyMappingUtils")
public class PropertyMappingUtils {
    @Autowired
    protected SimplePropertyValueProvider simplePropertyValueProvider;

    @Autowired
    protected CustomValueProvider customValueProvider;

    @Autowired
    protected Metadata metadata;

    public Map<String, Object> getPropertyValues(PropertyMappingContext context) {
        PropertyMapping mapping = context.getPropertyMapping();
        if (mapping instanceof ReferenceMultiFieldPropertyMapping) {
            MetaClass referenceMetaClass = context.getMetaProperty().getRange().asClass();
            return getPropertyValues((ReferenceMultiFieldPropertyMapping) mapping, context.getImportConfiguration(), context.getRawValuesSource(), referenceMetaClass);
        } else if (mapping instanceof ReferencePropertyMapping) {
            return Collections.singletonMap(((ReferencePropertyMapping) mapping).getLookupPropertyName(),
                    simplePropertyValueProvider.getValue(context));
        }
        return Collections.emptyMap();
    }

    protected Map<String, Object> getPropertyValues(ReferenceMultiFieldPropertyMapping multiFieldPropertyMapping,
                                                    ImportConfiguration importConfiguration,
                                                    RawValuesSource rawValuesSource,
                                                    MetaClass referencePropertyMetaClass) {
        Map<String, Object> propertyValues = new HashMap<>();
        List<String> lookupPropertyNames = multiFieldPropertyMapping.getLookupPropertyNames();
        multiFieldPropertyMapping.getReferencePropertyMappings()
                .stream()
                .filter(propertyMapping -> lookupPropertyNames.contains(propertyMapping.getEntityPropertyName()))
                .forEach(propertyMapping -> {
                    if (propertyMapping instanceof SimplePropertyMapping) {
                        PropertyMappingContext mappingContext = new PropertyMappingContext(propertyMapping)
                                .setRawValuesSource(rawValuesSource)
                                .setImportConfiguration(importConfiguration)
                                .setOwnerEntityMetaClass(referencePropertyMetaClass);
                        Object value = simplePropertyValueProvider.getValue(mappingContext);
                        propertyValues.put(propertyMapping.getEntityPropertyName(), value);
                    } else if (propertyMapping instanceof CustomPropertyMapping) {
                        Object value = customValueProvider.getValue((CustomPropertyMapping) propertyMapping, importConfiguration, rawValuesSource);
                        propertyValues.put(propertyMapping.getEntityPropertyName(), value);
                    }
                });
        return propertyValues;
    }
}

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

package io.jmix.dataimport.property.populator.impl;

import io.jmix.dataimport.configuration.ImportConfiguration;
import io.jmix.dataimport.configuration.mapping.CustomPropertyMapping;
import io.jmix.dataimport.extractor.data.RawValuesSource;
import io.jmix.dataimport.property.populator.CustomMappingContext;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component("datimp_CustomValueProvider")
public class CustomValueProvider {

    public Object getValue(CustomPropertyMapping propertyMapping,
                           ImportConfiguration importConfiguration,
                           RawValuesSource rawValuesSource) {
        Function<CustomMappingContext, Object> customValueFunction = propertyMapping.getCustomValueFunction();
        return customValueFunction.apply(new CustomMappingContext()
                .setRawValues(rawValuesSource.getRawValues())
                .setImportConfiguration(importConfiguration));
    }
}

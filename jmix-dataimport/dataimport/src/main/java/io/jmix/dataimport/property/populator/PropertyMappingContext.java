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

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.dataimport.configuration.ImportConfiguration;
import io.jmix.dataimport.configuration.mapping.PropertyMapping;
import io.jmix.dataimport.extractor.data.RawValuesSource;

import org.springframework.lang.Nullable;

/**
 * An object that contains info to get a result value of an entity property from the raw value
 */
public class PropertyMappingContext {
    protected ImportConfiguration importConfiguration;
    protected RawValuesSource rawValuesSource;
    protected MetaClass ownerEntityMetaClass;
    protected PropertyMapping propertyMapping;

    public PropertyMappingContext(PropertyMapping propertyMapping) {
        this.propertyMapping = propertyMapping;
    }

    public ImportConfiguration getImportConfiguration() {
        return importConfiguration;
    }

    public PropertyMappingContext setImportConfiguration(ImportConfiguration importConfiguration) {
        this.importConfiguration = importConfiguration;
        return this;
    }

    public RawValuesSource getRawValuesSource() {
        return rawValuesSource;
    }

    public PropertyMappingContext setRawValuesSource(RawValuesSource rawValuesSource) {
        this.rawValuesSource = rawValuesSource;
        return this;
    }

    public MetaClass getOwnerEntityMetaClass() {
        return ownerEntityMetaClass;
    }

    public PropertyMappingContext setOwnerEntityMetaClass(MetaClass ownerEntityMetaClass) {
        this.ownerEntityMetaClass = ownerEntityMetaClass;
        return this;
    }

    public PropertyMappingContext setPropertyMapping(PropertyMapping propertyMapping) {
        this.propertyMapping = propertyMapping;
        return this;
    }

    public PropertyMapping getPropertyMapping() {
        return propertyMapping;
    }

    @Nullable
    public Object getRawValue() {
        String dataFieldName = propertyMapping.getDataFieldName();
        if (dataFieldName == null) {
            return null;
        }
        return rawValuesSource.getRawValue(dataFieldName);
    }

    public MetaProperty getMetaProperty() {
        return ownerEntityMetaClass.getProperty(propertyMapping.getEntityPropertyName());
    }
}

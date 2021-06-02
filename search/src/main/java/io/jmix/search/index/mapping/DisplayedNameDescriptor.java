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

package io.jmix.search.index.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.search.index.mapping.strategy.DisplayedNameValueExtractor;
import io.jmix.search.index.mapping.strategy.FieldConfiguration;
import io.jmix.search.utils.Constants;

import java.util.Collections;
import java.util.List;

public class DisplayedNameDescriptor {

    protected FieldConfiguration fieldConfiguration;

    protected DisplayedNameValueExtractor propertyValueExtractor;

    protected List<MetaPropertyPath> instanceNameRelatedProperties;

    public String getIndexPropertyFullName() {
        return Constants.INSTANCE_NAME_FIELD;
    }

    public FieldConfiguration getFieldConfiguration() {
        return fieldConfiguration;
    }

    public void setFieldConfiguration(FieldConfiguration fieldConfiguration) {
        this.fieldConfiguration = fieldConfiguration;
    }

    public void setValueExtractor(DisplayedNameValueExtractor propertyValueExtractor) {
        this.propertyValueExtractor = propertyValueExtractor;
    }

    public List<MetaPropertyPath> getInstanceNameRelatedProperties() {
        return instanceNameRelatedProperties;
    }

    public void setInstanceNameRelatedProperties(List<MetaPropertyPath> instanceNameRelatedProperties) {
        this.instanceNameRelatedProperties = instanceNameRelatedProperties;
    }

    public JsonNode getValue(Object entity) {
        return propertyValueExtractor.getValue(entity, null, Collections.emptyMap());
    }
}

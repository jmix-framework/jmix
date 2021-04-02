/*
 * Copyright 2020 Haulmont.
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
package io.jmix.ui.xml.layout.loader;

import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.PropertyFilter;
import io.jmix.ui.component.PropertyFilter.Operation;
import io.jmix.ui.component.propertyfilter.PropertyFilterSupport;
import org.dom4j.Element;

import static io.jmix.core.querycondition.PropertyConditionUtils.generateParameterName;

public class PropertyFilterLoader extends AbstractSingleFilterComponentLoader<PropertyFilter<?>> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(PropertyFilter.NAME);
        loadId(resultComponent, element);
    }

    @Override
    protected void loadAttributesBeforeValueComponent() {
        super.loadAttributesBeforeValueComponent();

        loadString(element, "property", resultComponent::setProperty);
        loadEnum(element, Operation.class, "operation", resultComponent::setOperation);
        loadBoolean(element, "operationEditable", resultComponent::setOperationEditable);

        resultComponent.setParameterName(loadString(element, "parameterName")
                .orElse(generateParameterName(resultComponent.getProperty())));
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadBoolean(element, "operationCaptionVisible", resultComponent::setOperationCaptionVisible);
        loadDefaultValue(resultComponent, element);
    }

    @Override
    protected HasValue generateValueComponent() {
        MetaClass metaClass = resultComponent.getDataLoader().getContainer().getEntityMetaClass();
        return getSingleFilterSupport().generateValueComponent(metaClass,
                resultComponent.getProperty(), resultComponent.getOperation());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void loadDefaultValue(PropertyFilter component, Element element) {
        if (element.attribute("defaultValue") != null) {
            String defaultValue = element.attributeValue("defaultValue");
            MetaClass metaClass = component.getDataLoader().getContainer().getEntityMetaClass();
            MetaPropertyPath mpp = getMetadataTools().resolveMetaPropertyPathOrNull(metaClass, component.getProperty());
            if (mpp != null) {
                Object value = getPropertyFilterSupport().parseDefaultValue(mpp.getMetaProperty(),
                        component.getOperation().getType(), defaultValue);
                component.setValue(value);
            }
        }
    }

    protected PropertyFilterSupport getPropertyFilterSupport() {
        return applicationContext.getBean(PropertyFilterSupport.class);
    }

    protected MetadataTools getMetadataTools() {
        return applicationContext.getBean(MetadataTools.class);
    }
}

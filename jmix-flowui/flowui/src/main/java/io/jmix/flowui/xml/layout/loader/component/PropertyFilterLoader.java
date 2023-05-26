/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.component;

import com.vaadin.flow.component.Component;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.component.propertyfilter.PropertyFilter.Operation;
import io.jmix.flowui.component.propertyfilter.PropertyFilterSupport;
import io.jmix.flowui.component.propertyfilter.SingleFilterSupport;
import org.dom4j.Element;

import static io.jmix.core.querycondition.PropertyConditionUtils.generateParameterName;

public class PropertyFilterLoader extends AbstractSingleFilterComponentLoader<PropertyFilter> {

    @Override
    protected PropertyFilter createComponent() {
        return factory.create(PropertyFilter.class);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadDefaultValue(resultComponent, element);
    }

    @Override
    protected void loadAttributesBeforeValueComponent() {
        super.loadAttributesBeforeValueComponent();

        loadString(element, "property", resultComponent::setProperty);
        loadEnum(element, Operation.class, "operation", resultComponent::setOperation);
        loadBoolean(element, "operationEditable", resultComponent::setOperationEditable);
        loadBoolean(element, "operationTextVisible", resultComponent::setOperationTextVisible);

        String property = resultComponent.getProperty();
        if (property == null) {
            throw new RuntimeException("Property cannot be null");
        }
        resultComponent.setParameterName(loadString(element, "parameterName")
                .orElse(generateParameterName(property)));
    }

    @Override
    protected Component generateValueComponent() {
        MetaClass metaClass = resultComponent.getDataLoader().getContainer().getEntityMetaClass();
        String property = resultComponent.getProperty();
        if (property == null) {
            throw new RuntimeException("Property cannot be null");
        }

        return ((Component) getSingleFilterSupport().generateValueComponent(metaClass,
                resultComponent.getProperty(), resultComponent.getOperation()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void loadDefaultValue(PropertyFilter component, Element element) {
        loadString(element, "defaultValue")
                .map(defaultValue -> {
                    MetaClass metaClass = component.getDataLoader().getContainer().getEntityMetaClass();
                    String property = component.getProperty();
                    if (property == null) {
                        throw new RuntimeException("Property cannot be null");
                    }
                    MetaPropertyPath mpp = getMetadataTools().resolveMetaPropertyPathOrNull(metaClass, property);

                    return mpp != null
                            ? getPropertyFilterSupport().parseDefaultValue(mpp.getMetaProperty(),
                            component.getOperation().getType(), defaultValue)
                            : null;
                })
                .ifPresent(component::setValue);
    }

    protected SingleFilterSupport getSingleFilterSupport() {
        return applicationContext.getBean(SingleFilterSupport.class);
    }

    protected PropertyFilterSupport getPropertyFilterSupport() {
        return applicationContext.getBean(PropertyFilterSupport.class);
    }

    protected MetadataTools getMetadataTools() {
        return applicationContext.getBean(MetadataTools.class);
    }
}

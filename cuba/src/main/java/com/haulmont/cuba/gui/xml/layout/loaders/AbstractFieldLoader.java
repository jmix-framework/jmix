/*
 * Copyright 2019 Haulmont.
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

package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.cuba.gui.components.Field;
import com.haulmont.cuba.gui.xml.data.DatasourceLoaderHelper;
import io.jmix.core.metamodel.model.MetaProperty;
import org.dom4j.Element;

import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings({"rawtypes", "unchecked"})
@Deprecated
public abstract class AbstractFieldLoader<T extends Field>
        extends io.jmix.ui.xml.layout.loaders.AbstractFieldLoader<T> {

    @Override
    protected void loadValidators(io.jmix.ui.components.Field component, Element element) {
        loadValidators((Field) component, element);
    }

    @SuppressWarnings("unchecked")
    protected void loadValidators(Field component, Element element) {
        List<Element> validatorElements = element.elements("validator");

        if (!validatorElements.isEmpty()) {
            for (Element validatorElement : validatorElements) {
                Consumer<?> validator = loadValidator(validatorElement);
                if (validator != null) {
                    component.addValidator(validator);
                }
            }

        } else if (component.getDatasource() != null) {
            MetaProperty property = component.getMetaProperty();
            Consumer<?> validator = getDefaultValidator(property);
            if (validator != null) {
                component.addValidator(validator);
            }
        }
    }

    @Override
    protected ComponentLoaderContext getComponentContext() {
        return (ComponentLoaderContext) context;
    }

    @Override
    protected void loadData(T component, Element element) {
        super.loadData(component, element);

        DatasourceLoaderHelper
                .loadDatasourceIfValueSourceNull(resultComponent, element, context, getComponentContext())
                .ifPresent(component::setValueSource);
    }
}

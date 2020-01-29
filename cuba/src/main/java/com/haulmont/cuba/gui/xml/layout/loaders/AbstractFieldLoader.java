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

import com.haulmont.cuba.gui.components.DatasourceComponent;
import com.haulmont.cuba.gui.components.Field;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.web.gui.CubaUiComponents;
import io.jmix.core.BeanLocator;
import io.jmix.core.metamodel.datatypes.Datatypes;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.UiComponents;
import io.jmix.ui.components.Buffered;
import io.jmix.ui.components.HasDatatype;
import io.jmix.ui.components.validation.AbstractValidator;
import io.jmix.ui.components.validation.ValidatorLoadFactory;
import io.jmix.ui.components.validators.EmailValidator;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings({"rawtypes", "DeprecatedIsStillUsed"})
@Deprecated
public abstract class AbstractFieldLoader<T extends Field>
        extends io.jmix.ui.xml.layout.loaders.AbstractFieldLoader<T> {

    @Override
    public void setFactory(UiComponents factory) {
        // do nothing
    }

    @Override
    public void setBeanLocator(BeanLocator beanLocator) {
        super.setBeanLocator(beanLocator);

        // initialize with custom factory
        factory = beanLocator.get(CubaUiComponents.class);
    }

    @Override
    public void loadComponent() {
        assignFrame(resultComponent);
        assignXmlDescriptor(resultComponent, element);

        loadContainer(resultComponent, element);
        if (resultComponent.getValueSource() == null) {
            loadDatasource(resultComponent, element, getContext());
        }

        loadVisible(resultComponent, element);
        loadEditable(resultComponent, element);
        loadEnable(resultComponent, element);

        loadStyleName(resultComponent, element);

        loadCaption(resultComponent, element);
        loadIcon(resultComponent, element);
        loadDescription(resultComponent, element);
        loadContextHelp(resultComponent, element);

        loadValidators(resultComponent, element);
        loadValidation(resultComponent, element);

        loadRequired(resultComponent, element);

        loadHeight(resultComponent, element);
        loadWidth(resultComponent, element);
        loadAlign(resultComponent, element);
        loadResponsive(resultComponent, element);
        loadCss(resultComponent, element);
    }

    protected void loadRequired(Field component, Element element) {
        String required = element.attributeValue("required");
        if (StringUtils.isNotEmpty(required)) {
            component.setRequired(Boolean.parseBoolean(required));
        }

        String requiredMessage = element.attributeValue("requiredMessage");
        if (requiredMessage != null) {
            component.setRequiredMessage(loadResourceString(requiredMessage));
        }
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

    @SuppressWarnings("unchecked")
    protected void loadValidation(Field component, Element element) {
        Element validatorsHolder = element.element("validators");
        if (validatorsHolder != null) {
            List<Element> validators = validatorsHolder.elements();

            ValidatorLoadFactory loadFactory = beanLocator.get(ValidatorLoadFactory.NAME);

            for (Element validatorElem : validators) {
                AbstractValidator validator = loadFactory.createValidator(validatorElem, context.getMessagesPack());
                if (validator != null) {
                    component.addValidator(validator);
                } else if (validatorElem.getName().equals("email")) {
                    component.addValidator(new EmailValidator(validatorElem, context.getMessagesPack()));
                }
            }
        }
    }

    protected void loadBuffered(Buffered component, Element element) {
        String buffered = element.attributeValue("buffered");
        if (StringUtils.isNotEmpty(buffered)) {
            component.setBuffered(Boolean.parseBoolean(buffered));
        }
    }

    protected void loadDatatype(HasDatatype component, Element element) {
        String datatypeAttribute = element.attributeValue("datatype");
        if (StringUtils.isNotEmpty(datatypeAttribute)) {
            //noinspection unchecked
            component.setDatatype(Datatypes.get(datatypeAttribute));
        }
    }

    @Override
    protected ComponentLoaderContext getComponentContext() {
        return (ComponentLoaderContext) context;
    }

    protected void loadDatasource(DatasourceComponent component, Element element, ComponentLoader.Context context) {
        String datasource = element.attributeValue("datasource");
        if (!StringUtils.isEmpty(datasource)) {
            ComponentLoaderContext componentContext = getComponentContext();

            if (componentContext.getDsContext() == null) {
                throw new IllegalStateException("'datasource' attribute can be used only in screens with 'dsContext' element. " +
                        "In a screen with 'data' element use 'dataContainer' attribute.");
            }
            Datasource ds = componentContext.getDsContext().get(datasource);
            if (ds == null) {
                throw new GuiDevelopmentException(String.format("Datasource '%s' is not defined", datasource),
                        context, "Component ID", component.getId());
            }
            String property = element.attributeValue("property");
            if (StringUtils.isEmpty(property)) {
                throw new GuiDevelopmentException(
                        String.format("Can't set datasource '%s' for component '%s' because 'property' " +
                                "attribute is not defined", datasource, component.getId()), context);
            }

            component.setDatasource(ds, property);
        }
    }
}

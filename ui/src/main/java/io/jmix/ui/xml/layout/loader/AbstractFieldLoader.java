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
package io.jmix.ui.xml.layout.loader;

import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.ui.component.Buffered;
import io.jmix.ui.component.Field;
import io.jmix.ui.component.HasDatatype;
import io.jmix.ui.component.validation.Validator;
import io.jmix.ui.component.validation.ValidatorLoadFactory;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.List;

public abstract class AbstractFieldLoader<T extends Field> extends AbstractComponentLoader<T> {

    @Override
    public void loadComponent() {
        assignFrame(resultComponent);
        assignXmlDescriptor(resultComponent, element);

        loadData(resultComponent, element);

        loadVisible(resultComponent, element);
        loadEditable(resultComponent, element);
        loadEnable(resultComponent, element);

        loadStyleName(resultComponent, element);

        loadHtmlSanitizerEnabled(resultComponent, element);

        loadCaption(resultComponent, element);
        loadIcon(resultComponent, element);
        loadDescription(resultComponent, element);
        loadContextHelp(resultComponent, element);

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
    protected void loadValidation(Field component, Element element) {
        Element validatorsHolder = element.element("validators");
        if (validatorsHolder != null) {
            List<Element> validators = validatorsHolder.elements();

            ValidatorLoadFactory loadFactory = (ValidatorLoadFactory) applicationContext.getBean(ValidatorLoadFactory.NAME);

            for (Element validatorElem : validators) {
                Validator validator = loadFactory.createValidator(validatorElem, context.getMessagesPack());
                if (validator != null) {
                    component.addValidator(validator);
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
            DatatypeRegistry datatypeRegistry = (DatatypeRegistry) applicationContext.getBean(DatatypeRegistry.NAME);
            component.setDatatype(datatypeRegistry.find(datatypeAttribute));
        }
    }
}

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

package io.jmix.dynattrui.impl;

import io.jmix.core.BeanLocator;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.ui.component.validation.*;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Component(AttributeValidators.NAME)
public class AttributeValidators {
    public static final String NAME = "dynattrui_AttributeValidators";

    @Autowired
    protected BeanLocator beanLocator;

    /**
     * Returns validators for a dynamic attribute
     *
     * @return collection of validators
     */
    public Collection<Consumer<?>> getValidators(AttributeDefinition attribute) {
        List<Consumer<?>> validators;

        switch (attribute.getDataType()) {
            case INTEGER:
                validators = createIntegerValidators(attribute);
                break;
            case DOUBLE:
                validators = createDoubleValidators(attribute);
                break;
            case DECIMAL:
                validators = createDecimalValidators(attribute);
                break;
            default:
                validators = new ArrayList<>();
        }

        // add custom groovy script validator
        if (attribute.getConfiguration().getValidatorGroovyScript() != null) {
            GroovyScriptValidator validator = beanLocator.getPrototype(GroovyScriptValidator.NAME,
                    attribute.getConfiguration().getValidatorGroovyScript());
            validators.add(validator);
        }

        return validators;
    }

    protected List<Consumer<?>> createIntegerValidators(AttributeDefinition attribute) {
        List<Consumer<?>> validators = new ArrayList<>();
        if (attribute.getConfiguration().getMinInt() != null) {
            validators.add(beanLocator.getPrototype(MinValidator.NAME, attribute.getConfiguration().getMinInt()));
        }
        if (attribute.getConfiguration().getMaxInt() != null) {
            validators.add(beanLocator.getPrototype(MaxValidator.NAME, attribute.getConfiguration().getMaxInt()));
        }
        return validators;
    }

    protected List<Consumer<?>> createDoubleValidators(AttributeDefinition attribute) {
        List<Consumer<?>> validators = new ArrayList<>();
        if (attribute.getConfiguration().getMinDouble() != null) {
            validators.add(beanLocator.getPrototype(DoubleMinValidator.NAME, attribute.getConfiguration().getMinDouble()));
        }
        if (attribute.getConfiguration().getMaxDouble() != null) {
            validators.add(beanLocator.getPrototype(DoubleMaxValidator.NAME, attribute.getConfiguration().getMaxDouble()));
        }
        return validators;
    }

    protected List<Consumer<?>> createDecimalValidators(AttributeDefinition attribute) {
        List<Consumer<?>> validators = new ArrayList<>();
        if (attribute.getConfiguration().getMinDecimal() != null) {
            validators.add(beanLocator.getPrototype(DecimalMinValidator.NAME, attribute.getConfiguration().getMinDecimal()));
        }
        if (attribute.getConfiguration().getMaxDecimal() != null) {
            validators.add(beanLocator.getPrototype(DecimalMaxValidator.NAME, attribute.getConfiguration().getMaxDecimal()));
        }
        return validators;
    }
}

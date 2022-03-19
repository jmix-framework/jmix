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

import io.jmix.dynattr.AttributeDefinition;
import io.jmix.ui.component.validation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component("dynat_AttributeValidators")
public class AttributeValidators {

    @Autowired
    protected ApplicationContext applicationContext;

    /**
     * Returns validators for a dynamic attribute
     *
     * @return collection of validators
     */
    public Collection<Validator<?>> getValidators(AttributeDefinition attribute) {
        List<Validator<?>> validators;

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
            GroovyScriptValidator validator = applicationContext.getBean(GroovyScriptValidator.class,
                    attribute.getConfiguration().getValidatorGroovyScript());
            validators.add(validator);
        }

        return validators;
    }

    protected List<Validator<?>> createIntegerValidators(AttributeDefinition attribute) {
        List<Validator<?>> validators = new ArrayList<>();
        if (attribute.getConfiguration().getMinInt() != null) {
            validators.add((Validator<?>) applicationContext.getBean(MinValidator.class, attribute.getConfiguration().getMinInt()));
        }
        if (attribute.getConfiguration().getMaxInt() != null) {
            validators.add((Validator<?>) applicationContext.getBean(MaxValidator.class, attribute.getConfiguration().getMaxInt()));
        }
        return validators;
    }

    protected List<Validator<?>> createDoubleValidators(AttributeDefinition attribute) {
        List<Validator<?>> validators = new ArrayList<>();
        if (attribute.getConfiguration().getMinDouble() != null) {
            validators.add((Validator<?>) applicationContext.getBean(DoubleMinValidator.class, attribute.getConfiguration().getMinDouble()));
        }
        if (attribute.getConfiguration().getMaxDouble() != null) {
            validators.add((Validator<?>) applicationContext.getBean(DoubleMaxValidator.class, attribute.getConfiguration().getMaxDouble()));
        }
        return validators;
    }

    protected List<Validator<?>> createDecimalValidators(AttributeDefinition attribute) {
        List<Validator<?>> validators = new ArrayList<>();
        if (attribute.getConfiguration().getMinDecimal() != null) {
            validators.add((Validator<?>) applicationContext.getBean(DecimalMinValidator.class, attribute.getConfiguration().getMinDecimal()));
        }
        if (attribute.getConfiguration().getMaxDecimal() != null) {
            validators.add((Validator<?>) applicationContext.getBean(DecimalMaxValidator.class, attribute.getConfiguration().getMaxDecimal()));
        }
        return validators;
    }
}

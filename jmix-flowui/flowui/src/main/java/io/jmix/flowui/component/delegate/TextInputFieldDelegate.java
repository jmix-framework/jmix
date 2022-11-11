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

package io.jmix.flowui.component.delegate;

import com.vaadin.flow.component.AbstractField;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.data.binding.impl.AbstractValueBinding;
import io.jmix.flowui.data.binding.impl.FieldValueBinding;
import io.jmix.flowui.data.EntityValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.component.validation.RegexpValidator;
import io.jmix.flowui.component.validation.SizeValidator;
import io.jmix.flowui.component.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("flowui_TextFieldDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TextInputFieldDelegate<C extends AbstractField<?, String>, V> extends AbstractFieldDelegate<C, V, String> {

    private static final Logger log = LoggerFactory.getLogger(TextInputFieldDelegate.class);

    protected SizeValidator<? super V> sizeValidator;
    protected RegexpValidator regexpValidator;

    protected String pattern;
    protected int maxLength = -1;
    protected int minLength = -1;

    public TextInputFieldDelegate(C component) {
        super(component);
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;

        setupRegexpValidation();
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;

        setupSizeValidation();
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;

        setupSizeValidation();
    }

    protected void setupRegexpValidation() {
        if (isStringDatatype()) {
            if (regexpValidator == null) {
                regexpValidator = createRegexpValidator(pattern);
                addValidator((Validator<V>) regexpValidator);
            } else {
                regexpValidator.setRegexp(pattern);
            }
        } else {
            log.debug("{} is not added because component value type is not {}",
                    RegexpValidator.class, String.class.getSimpleName());
        }
    }

    protected void setupSizeValidation() {
        if (isStringDatatype()) {
            if (sizeValidator == null) {
                sizeValidator = createSizeValidator();
                addValidator(sizeValidator);
            }
            if (getMinLength() != -1) {
                sizeValidator.setMin(getMinLength());
            }
            if (getMaxLength() != -1) {
                sizeValidator.setMax(getMaxLength());
            }
        } else {
            log.debug("{} is not added because component value type is not {}",
                    SizeValidator.class, String.class.getSimpleName());
        }
    }

    protected boolean isStringDatatype() {
        if (getDatatype() != null) {
            return String.class.isAssignableFrom(getDatatype().getJavaClass());
        }

        if (getValueSource() instanceof EntityValueSource) {
            MetaPropertyPath metaPropertyPath = ((EntityValueSource<?, V>) getValueSource()).getMetaPropertyPath();
            MetaProperty metaProperty = metaPropertyPath.getMetaProperty();
            return String.class.isAssignableFrom(metaProperty.getJavaType());
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractValueBinding<V> createValueBinding(ValueSource<V> valueSource) {
        return applicationContext.getBean(FieldValueBinding.class, valueSource, component);
    }

    @SuppressWarnings("unchecked")
    protected SizeValidator<V> createSizeValidator() {
        return applicationContext.getBean(SizeValidator.class);
    }

    protected RegexpValidator createRegexpValidator(String pattern) {
        return applicationContext.getBean(RegexpValidator.class, pattern);
    }
}

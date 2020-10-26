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

package com.haulmont.cuba.gui.components.validators;

import com.haulmont.cuba.core.global.AppBeans;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.ui.component.Field;
import io.jmix.ui.component.validator.AbstractBeanValidator;
import io.jmix.ui.component.validator.BeanPropertyValidator;

import javax.validation.Validator;


/**
 * @deprecated Use {@link BeanPropertyValidator} instead
 */
@Deprecated
public class BeanValidator extends AbstractBeanValidator {
    public BeanValidator(Class beanClass, String beanProperty) {
        super(beanClass, beanProperty);

        init();
    }

    public BeanValidator(Class beanClass, String beanProperty, Class[] validationGroups) {
        super(beanClass, beanProperty, validationGroups);

        init();
    }

    protected void init() {
        this.messages = AppBeans.get(Messages.class);
        this.metadata = AppBeans.get(Metadata.class);
        this.validator = AppBeans.get(Validator.class);
    }
}
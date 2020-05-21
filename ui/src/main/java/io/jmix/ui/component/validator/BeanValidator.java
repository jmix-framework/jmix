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

package io.jmix.ui.component.validator;

import io.jmix.core.AppBeans;
import io.jmix.core.BeanValidation;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.ui.component.Field;

/**
 * Validator that applies JSR303 rules for {@link Field} instance using {@link BeanValidation}.
 *
 * @deprecated Use {@link BeanPropertyValidator} instead.
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
        this.messages = AppBeans.get(Messages.NAME);
        this.metadata = AppBeans.get(Metadata.NAME);
        this.beanValidation = AppBeans.get(BeanValidation.NAME);
    }
}
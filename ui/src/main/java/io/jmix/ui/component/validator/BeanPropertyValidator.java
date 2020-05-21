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

import io.jmix.core.BeanValidation;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.ui.component.HasValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Validator that applies JSR 303 rules for {@link HasValue} instance using {@link BeanValidation}. <br>
 * Automatically added on data binding if property enclosing class has {@link BeanValidation} constraints.
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Component(BeanPropertyValidator.NAME)
public class BeanPropertyValidator extends AbstractBeanValidator {

    public static final String NAME = "jmix_BeanPropertyValidator";

    public BeanPropertyValidator(Class beanClass, String beanProperty) {
        super(beanClass, beanProperty);
    }

    public BeanPropertyValidator(Class beanClass, String beanProperty, Class[] validationGroups) {
        super(beanClass, beanProperty, validationGroups);
    }

    @Autowired
    protected void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    protected void setMessageTools(MessageTools messageTools) {
        this.messageTools = messageTools;
    }

    @Autowired
    protected void setBeanValidation(BeanValidation beanValidation) {
        this.beanValidation = beanValidation;
    }
}

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

package io.jmix.flowui.component.validation.bean;

import com.vaadin.flow.component.HasValue;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.flowui.SameAsUi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.Validator;

/**
 * Validator that applies JSR 303 rules for {@link HasValue} instance. <br>
 * Automatically added on data binding if property enclosing class has validation constraints.
 */

@SameAsUi
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Component("flowui_BeanPropertyValidator")
public class BeanPropertyValidator extends AbstractBeanValidator {

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
    protected void setValidator(Validator validator) {
        this.validator = validator;
    }
}

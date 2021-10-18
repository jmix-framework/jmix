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

import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.Validator;

/**
 * Validator that applies JSR 303 rules for {@link HasValue} instance. <br>
 * Automatically added on data binding if property enclosing class has validation constraints.
 */
@StudioElement(
        caption = "CustomValidator",
        xmlElement = "custom",
        unsupportedTarget = {"io.jmix.ui.component.CheckBox", "io.jmix.ui.component.ColorPicker",
                "io.jmix.ui.component.CurrencyField", "io.jmix.ui.component.DatePicker",
                "io.jmix.ui.component.SingleFileUploadField", "io.jmix.ui.component.Slider",
                "io.jmix.searchui.component.SearchField"},
        icon = "io/jmix/ui/icon/element/validator.svg"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "bean", type = PropertyType.BEAN_REF, required = true,
                        options = "io.jmix.ui.component.validation.Validator"),
                @StudioProperty(name = "message", type = PropertyType.LOCALIZED_STRING)
        }
)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Component("ui_BeanPropertyValidator")
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

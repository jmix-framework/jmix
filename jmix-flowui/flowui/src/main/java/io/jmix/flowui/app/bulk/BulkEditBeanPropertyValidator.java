/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.app.bulk;

import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.component.validation.bean.BeanPropertyValidator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Component("flowui_BulkEditBeanPropertyValidator")
public class BulkEditBeanPropertyValidator implements Validator<Object> {

    protected final Class<?> beanClass;
    protected final String beanProperty;

    protected ObjectProvider<BeanPropertyValidator> delegateProvider;
    protected BeanPropertyValidator delegate;

    public BulkEditBeanPropertyValidator(Class<?> beanClass, String beanProperty) {
        this.beanClass = beanClass;
        this.beanProperty = beanProperty;
    }

    @Autowired
    public void setDelegateProvider(ObjectProvider<BeanPropertyValidator> delegateProvider) {
        this.delegateProvider = delegateProvider;
    }

    @Override
    public void accept(@Nullable Object value) {
        if (value != null) {
            getDelegate().accept(value);
        }
    }

    protected BeanPropertyValidator getDelegate() {
        return delegate != null ? delegate : delegateProvider.getObject(beanClass, beanProperty);
    }
}

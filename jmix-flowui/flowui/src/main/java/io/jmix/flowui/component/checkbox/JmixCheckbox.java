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

package io.jmix.flowui.component.checkbox;

import com.vaadin.flow.component.checkbox.Checkbox;
import io.jmix.flowui.component.delegate.FieldDelegate;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;

public class JmixCheckbox extends Checkbox implements SupportsValueSource<Boolean>, ApplicationContextAware,
        InitializingBean {

    protected ApplicationContext applicationContext;

    protected FieldDelegate<JmixCheckbox, Boolean, Boolean> fieldDelegate;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        initComponent();
    }

    protected void initComponent() {
        fieldDelegate = createFieldDelegate();
    }

    protected FieldDelegate<JmixCheckbox, Boolean, Boolean> createFieldDelegate() {
        return applicationContext.getBean(FieldDelegate.class, this);
    }

    @Nullable
    @Override
    public ValueSource<Boolean> getValueSource() {
        return fieldDelegate.getValueSource();
    }

    @Override
    public void setValueSource(@Nullable ValueSource<Boolean> valueSource) {
        fieldDelegate.setValueSource(valueSource);
    }

    @Override
    public void setValue(Boolean value) {
        super.setValue(BooleanUtils.toBoolean(value));
    }
}

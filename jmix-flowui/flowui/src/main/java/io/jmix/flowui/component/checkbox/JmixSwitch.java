/*
 * Copyright 2025 Haulmont.
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

import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.checkbox.Switch;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class JmixSwitch extends Switch
        implements SupportsValueSource<Boolean>, SupportsValidation<Boolean>,
        HasRequired, ApplicationContextAware, InitializingBean {

    // TODO: kd, remove all

    @Override
    public String getRequiredMessage() {
        return "";
    }

    @Override
    public void setRequiredMessage(String requiredMessage) {

    }

    @Override
    public Registration addValidator(Validator<? super Boolean> validator) {
        return null;
    }

    @Override
    public void executeValidators() throws ValidationException {

    }

    @Override
    public void setInvalid(boolean invalid) {

    }

    @Override
    public ValueSource<Boolean> getValueSource() {
        return null;
    }

    @Override
    public void setValueSource(ValueSource<Boolean> valueSource) {

    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }
}

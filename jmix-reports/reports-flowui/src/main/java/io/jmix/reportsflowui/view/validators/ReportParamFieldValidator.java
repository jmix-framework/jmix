/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reportsflowui.view.validators;

import io.jmix.core.DevelopmentException;
import io.jmix.flowui.component.validation.AbstractValidator;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.exception.ReportParametersValidationException;
import io.jmix.reports.exception.ValidationException;
import io.jmix.reportsflowui.view.ReportParameterValidator;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("report_ReportParamFieldValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReportParamFieldValidator extends AbstractValidator<Object> {

    @Autowired
    protected ReportParameterValidator reportParameterValidator;

    protected final ReportInputParameter inputParameter;

    public ReportParamFieldValidator(ReportInputParameter inputParameter) {
        if (inputParameter == null) {
            throw new DevelopmentException("ReportInputParameter is not defined");
        }

        this.inputParameter = inputParameter;
    }

    @Override
    public void accept(@Nullable Object value) {
        if (value != null) {
            try {
                reportParameterValidator.validateParameterValue(inputParameter, value);
            } catch (ReportParametersValidationException e) {
                throw new ValidationException(e.getMessage());
            }
        }
    }
}

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

import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.validation.AbstractValidator;
import io.jmix.reports.exception.ValidationException;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("report_ReportCollectionValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReportCollectionValidator extends AbstractValidator<Object> {

    protected HasRequired field;

    public ReportCollectionValidator(HasRequired field) {
        this.field = field;
    }

    @Override
    public void accept(@Nullable Object value) {
        if (field != null && ObjectUtils.isEmpty(value)) {
            throw new ValidationException(field.getRequiredMessage());
        }
    }
}

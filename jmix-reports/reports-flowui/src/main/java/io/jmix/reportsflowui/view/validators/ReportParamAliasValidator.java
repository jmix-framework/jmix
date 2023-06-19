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

import com.google.common.base.Strings;
import io.jmix.flowui.component.validation.AbstractValidator;
import io.jmix.flowui.exception.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("report_ReportParamAliasValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReportParamAliasValidator extends AbstractValidator<String> {

    @Override
    public void accept(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return;
        }

        if (!value.matches("[\\w]*")) {
            String incorrectParamName = messages.getMessage(ReportParamAliasValidator.class, "incorrectParamAlias");
            throw new ValidationException(incorrectParamName);
        }

        if (value.equals("_")) {
            String incorrectParamName = messages.getMessage(ReportParamAliasValidator.class, "notOnlyUnderscore");
            throw new ValidationException(incorrectParamName);
        }
    }
}

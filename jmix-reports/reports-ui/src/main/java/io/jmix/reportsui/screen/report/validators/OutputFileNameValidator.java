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

package io.jmix.reportsui.screen.report.validators;

import io.jmix.core.Messages;
import io.jmix.reportsui.screen.report.wizard.ReportWizardCreator;
import io.jmix.ui.component.ValidationException;
import io.jmix.ui.component.validation.AbstractValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Component("report_OutputFileNameValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OutputFileNameValidator extends AbstractValidator<String> {

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Override
    public void accept(String value) {
        if (StringUtils.isNotEmpty(value) && !value.matches("^[^/:*<>?\\\\]*$"))
            throw new ValidationException(String.format(
                    messages.getMessage(getClass(), "fillCorrectOutputFileNameMsg"),
                    messages.getMessage(ReportWizardCreator.class, "outputFileName")));
    }
}

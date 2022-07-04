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

package io.jmix.flowui.component.validation;

import io.jmix.core.TimeSource;
import io.jmix.flowui.component.validation.time.TimeValidator;
import io.jmix.flowui.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component("flowui_FutureOrPresentValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FutureOrPresentValidator<T> extends AbstractValidator<T> {

    protected TimeSource timeSource;

    protected boolean checkSeconds = false;

    public FutureOrPresentValidator() {
    }

    /**
     * Constructor for custom error message.
     *
     * @param message error message
     */
    public FutureOrPresentValidator(String message) {
        this.message = message;
    }

    @Autowired
    protected void setTimeSource(TimeSource timeSource) {
        this.timeSource = timeSource;
    }

    /**
     * Set true if validator should also check seconds and nanos (if supported) in value. Default value is false.
     *
     * @param checkSeconds check seconds
     */
    public void setCheckSeconds(boolean checkSeconds) {
        this.checkSeconds = checkSeconds;
    }

    /**
     * @return true if seconds and nanos are checked
     */
    public boolean isCheckSeconds() {
        return checkSeconds;
    }

    @Override
    public void accept(@Nullable T value) throws ValidationException {
        // consider null value is valid
        if (value == null) {
            return;
        }

        TimeValidator timeConstraint = ValidatorHelper.getTimeConstraint(timeSource, value);
        if (timeConstraint == null) {
            throw new IllegalArgumentException(
                    "FutureOrPresentValidator doesn't support following type: '" + value.getClass() + "'");
        }

        timeConstraint.setCheckSeconds(checkSeconds);
        if (!timeConstraint.isFutureOrPresent()) {
            String message = getMessage();
            this.defaultMessage = messages.getMessage("validation.constraints.futureOrPresent");

            fireValidationException(message == null ? defaultMessage : message);
        }
    }
}

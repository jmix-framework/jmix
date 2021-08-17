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

package io.jmix.ui.component.validation;


import io.jmix.core.DateTimeTransformations;
import io.jmix.core.Messages;
import io.jmix.core.TimeSource;
import io.jmix.ui.component.ValidationException;
import io.jmix.ui.component.validation.time.TimeValidator;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Date;

/**
 * Validates that date or time in the future.
 * <p>
 * Note, types that support TimeZones can be found in {@link DateTimeTransformations#isDateTypeSupportsTimeZones(Class)}.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in configuration class,
 * for example:
 * <pre>
 *     &#64;Bean("ui_FutureValidator")
 *     &#64;Scope(BeanDefinition.SCOPE_PROTOTYPE)
 *     &#64;Primary
 *     protected FutureValidator futureValidator() {
 *          return new CustomFutureValidator();
 *     }
 * </pre>
 *
 * @param <T> {@link Date}, {@link LocalDate}, {@link LocalDateTime}, {@link LocalTime}, {@link OffsetDateTime},
 *            {@link OffsetTime}
 */
@StudioElement(
        caption = "FutureValidator",
        xmlElement = "future",
        target = {"io.jmix.ui.component.DateField", "io.jmix.ui.component.TimeField"},
        icon = "io/jmix/ui/icon/element/validator.svg"
)
@Component("ui_FutureValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FutureValidator<T> extends AbstractValidator<T> {

    protected TimeSource timeSource;

    protected boolean checkSeconds = false;

    public FutureValidator() {
    }

    /**
     * Constructor for custom error message.
     *
     * @param message error message
     */
    public FutureValidator(String message) {
        this.message = message;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
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
    @StudioProperty
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
    public void accept(T value) throws ValidationException {
        // consider null value is valid
        if (value == null) {
            return;
        }

        TimeValidator timeConstraint = ValidatorHelper.getTimeConstraint(timeSource, value);
        if (timeConstraint == null) {
            throw new IllegalArgumentException("FutureValidator doesn't support following type: '" + value.getClass() + "'");
        }

        timeConstraint.setCheckSeconds(checkSeconds);
        if (!timeConstraint.isFuture()) {
            String message = getMessage();
            if (message == null) {
                message = messages.getMessage("validation.constraints.future");
            }

            throw new ValidationException(message);
        }
    }
}

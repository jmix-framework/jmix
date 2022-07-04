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

import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.flowui.exception.ValidationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Map;

@Component("flowui_DateTimeRangeValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DateTimeRangeValidator<T extends Comparable<T>> extends AbstractValidator<T> {

    protected T min;
    protected T max;

    public DateTimeRangeValidator() {
    }

    public DateTimeRangeValidator(String message) {
        this.message = message;
    }

    @Nullable
    public T getMin() {
        return min;
    }

    public void setMin(@Nullable T min) {
        this.min = min;
    }

    @Nullable
    public T getMax() {
        return max;
    }

    public void setMax(@Nullable T max) {
        this.max = max;
    }

    @Override
    public void accept(@Nullable T value) throws ValidationException {
        // consider null value is in range
        if (value == null) {
            return;
        }

        if (min != null && value.compareTo(min) < 0) {
            Datatype<?> datatype = datatypeRegistry.get(min.getClass());
            String formatted = datatype.format(min, currentAuthentication.getLocale());

            this.defaultMessage = messages.getMessage("validation.constraints.dateTimeRangeMinExceeded");

            fireValidationException(
                    message == null ? defaultMessage : message,
                    Map.of("min", formatted));
        }

        if (max != null && value.compareTo(max) > 0) {
            Datatype<?> datatype = datatypeRegistry.get(max.getClass());
            String formatted = datatype.format(min, currentAuthentication.getLocale());

            this.defaultMessage = messages.getMessage("validation.constraints.dateTimeRangeMaxExceeded");

            fireValidationException(
                    message == null ? defaultMessage : message,
                    Map.of("max", formatted));
        }
    }
}

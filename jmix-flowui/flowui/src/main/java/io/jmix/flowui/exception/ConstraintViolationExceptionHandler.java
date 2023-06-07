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

package io.jmix.flowui.exception;

import io.jmix.core.MessageTools;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.view.ViewValidation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Handles ConstraintViolationException that can be thrown by bean validation on persistence layer.
 * Displays violation messages as screen notifications.
 */
@Component
public class ConstraintViolationExceptionHandler extends AbstractUiExceptionHandler {

    @Autowired
    protected Notifications notifications;
    @Autowired
    protected ViewValidation viewValidation;
    @Autowired
    protected MessageTools messageTools;

    public ConstraintViolationExceptionHandler() {
        super(ConstraintViolationException.class.getName());
    }

    @Override
    protected void doHandle(String className, String message, Throwable throwable) {
        ConstraintViolationException exception = (ConstraintViolationException) throwable;
        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        ValidationErrors validationErrors = new ValidationErrors();
        violations.forEach(v -> {
            String violationMessage = v.getMessage();
            if (violationMessage.startsWith("{") && violationMessage.endsWith("}")) {
                violationMessage = violationMessage.substring(1, violationMessage.length() - 1);
                if (messageTools.isMessageKey(violationMessage)) {
                    violationMessage = messageTools.loadString(violationMessage);
                }
            }
            validationErrors.add(violationMessage);
        });

        viewValidation.showValidationErrors(validationErrors);
    }
}

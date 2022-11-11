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

package io.jmix.flowui.component.validation.bean;

import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.validation.group.UiComponentChecks;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.exception.CompositeValidationException;

import javax.validation.ConstraintViolation;
import javax.validation.groups.Default;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AbstractBeanValidator implements Validator {

    protected Class beanClass;
    protected String beanProperty;

    protected String validationErrorMessage;
    protected Class[] validationGroups;

    protected Metadata metadata;
    protected Messages messages;
    protected MessageTools messageTools;
    protected javax.validation.Validator validator;

    protected AbstractBeanValidator(Class beanClass, String beanProperty) {
        this.beanClass = beanClass;
        this.beanProperty = beanProperty;
    }

    protected AbstractBeanValidator(Class beanClass, String beanProperty, Class[] validationGroups) {
        this.beanClass = beanClass;
        this.beanProperty = beanProperty;
        this.validationGroups = validationGroups;
    }

    public Class[] getValidationGroups() {
        return validationGroups;
    }

    /**
     * Set custom validation groups. If not set validator uses {@link Default} and {@link UiComponentChecks} groups.
     *
     * @param validationGroups validation groups
     */
    public void setValidationGroups(Class[] validationGroups) {
        this.validationGroups = validationGroups;
    }

    public String getValidationErrorMessage() {
        return validationErrorMessage;
    }

    /**
     * Set main validation error message. Useful only for custom validation in view controller.
     *
     * @param validationErrorMessage validation error message
     */
    public void setValidationErrorMessage(String validationErrorMessage) {
        this.validationErrorMessage = validationErrorMessage;
    }

    @Override
    public void accept(Object value) {
        Class[] groups = this.validationGroups;
        if (groups == null || groups.length == 0) {
            groups = new Class[]{Default.class, UiComponentChecks.class};
        }

        if (metadata.getClass(beanClass).findProperty(beanProperty) != null) {
            @SuppressWarnings("unchecked")
            Set<ConstraintViolation> violations = validator.validateValue(beanClass, beanProperty, value, groups);

            if (!violations.isEmpty()) {
                List<CompositeValidationException.ViolationCause> causes = new ArrayList<>(violations.size());
                for (ConstraintViolation violation : violations) {
                    causes.add(new BeanPropertyValidator.BeanValidationViolationCause(violation));
                }

                String validationMessage = this.validationErrorMessage;
                if (validationMessage == null) {
                    validationMessage = getDefaultErrorMessage();
                }

                throw new CompositeValidationException(validationMessage, causes);
            }
        }
    }

    public String getDefaultErrorMessage() {
        MetaClass metaClass = metadata.getClass(beanClass);

        return messages.formatMessage("", "validation.defaultMessage",
                messageTools.getPropertyCaption(metaClass, beanProperty));
    }

    public static class BeanValidationViolationCause implements CompositeValidationException.ViolationCause {
        protected ConstraintViolation constraintViolation;

        public BeanValidationViolationCause(ConstraintViolation constraintViolation) {
            this.constraintViolation = constraintViolation;
        }

        @Override
        public String getMessage() {
            return constraintViolation.getMessage();
        }
    }
}

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

package com.haulmont.cuba.core.global.validation;

import com.haulmont.cuba.core.global.AppBeans;
import io.jmix.core.EntityStates;
import io.jmix.core.Entity;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;

public class RequiredViewValidator implements ConstraintValidator<RequiredView, Object> {

    private String view;

    @Override
    public void initialize(RequiredView annotation) {
        this.view = annotation.value();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            EntityStates entityStates = AppBeans.get(EntityStates.class);
            if (value instanceof Entity) {
                entityStates.checkLoadedWithFetchPlan((Entity) value, view);
            } else if (value instanceof Collection) {
                @SuppressWarnings("unchecked")
                Collection<Entity> entities = (Collection<Entity>) value;
                for (Entity entity : entities) {
                    entityStates.checkLoadedWithFetchPlan(entity, view);
                }
            }

            return true;
        } catch (IllegalArgumentException e) {
            LoggerFactory.getLogger(RequiredViewValidator.class)
                    .debug("Failed validation of instance with required view: {}", e.getMessage());

            return false;
        }
    }
}

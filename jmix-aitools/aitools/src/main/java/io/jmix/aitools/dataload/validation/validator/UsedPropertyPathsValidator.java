/*
 * Copyright 2026 Haulmont.
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

package io.jmix.aitools.dataload.validation.validator;

import io.jmix.aitools.introspection.introspector.JpaDomainModelIntrospector;
import io.jmix.core.JmixOrder;
import io.jmix.aitools.dataload.generation.GeneratedJpqlResult;
import io.jmix.aitools.dataload.validation.JpqlResultValidator;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("textdt_UsedPropertyPathsValidator")
public class UsedPropertyPathsValidator implements JpqlResultValidator, Ordered {

    @Autowired
    protected JpaDomainModelIntrospector modelIntrospector;

    @Override
    public List<JpqlValidationIssue> validate(GeneratedJpqlResult result) {
        String rootEntityName = result.getRootEntityName();
        if (rootEntityName == null || rootEntityName.isBlank() || !modelIntrospector.containsEntity(rootEntityName)) {
            return List.of();
        }

        List<JpqlValidationIssue> issues = new ArrayList<>();

        for (String propertyPath : result.getUsedPropertyPaths()) {
            if (!modelIntrospector.containsPropertyPath(rootEntityName, propertyPath)) {
                issues.add(new JpqlValidationIssue("propertyPath.invalid",
                        "Invalid property path for root entity " + rootEntityName + ": " + propertyPath));
            }
        }

        return issues;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 1000;
    }
}

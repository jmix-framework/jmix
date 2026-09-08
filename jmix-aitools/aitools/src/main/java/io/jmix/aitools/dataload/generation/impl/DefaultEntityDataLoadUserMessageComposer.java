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

package io.jmix.aitools.dataload.generation.impl;

import io.jmix.aitools.dataload.generation.EntityDataLoadGenerationRequest;
import io.jmix.aitools.dataload.generation.EntityDataLoadQueryParameter;
import io.jmix.aitools.dataload.generation.EntityDataLoadUserMessageComposer;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Default {@link EntityDataLoadUserMessageComposer}. Phrases the constraints from separate protected pieces so an
 * application can override one at a time.
 */
@NullMarked
@Component("aitls_EntityDataLoadUserMessageComposer")
public class DefaultEntityDataLoadUserMessageComposer implements EntityDataLoadUserMessageComposer {

    @Override
    public String composeConstraints(EntityDataLoadGenerationRequest request) {
        return composeParametersSection(request.getAvailableParameters())
                + composeRequiredResultPropertiesSection(request.getRequiredResultProperties());
    }

    protected String composeParametersSection(List<EntityDataLoadQueryParameter> parameters) {
        if (parameters.isEmpty()) {
            return "";
        }

        StringBuilder text = new StringBuilder("\n\nAVAILABLE PARAMETERS:");
        boolean anyOptional = false;
        for (EntityDataLoadQueryParameter parameter : parameters) {
            text.append("\n- :").append(parameter.getName()).append(" (").append(parameter.getJavaType());
            if (parameter.isMultiValued()) {
                text.append(", several values of this type, matched with IN and no parentheses ")
                        .append("around the parameter name");
            }
            if (parameter.isOptional()) {
                anyOptional = true;
                text.append(", may be null");
            }
            text.append(')');
        }

        text.append("\n\nPARAMETER RULES:")
                .append("\n- Reference these as JPQL named parameters; never inline their values.")
                .append("\n- Use a parameter only where the request calls for it; ignore the rest.");
        if (anyOptional) {
            text.append("\n- A parameter marked \"may be null\" must not be compared directly. Wrap its ")
                    .append("whole condition so that a null value switches the condition off, like this: ")
                    .append("(:name is null or e.attribute = :name). Write that guard for every condition ")
                    .append("that uses such a parameter.");
        }
        return text.toString();
    }

    protected String composeRequiredResultPropertiesSection(List<String> requiredResultProperties) {
        if (requiredResultProperties.isEmpty()) {
            return "";
        }

        StringBuilder text = new StringBuilder("\n\nREQUIRED RESULT COLUMNS: the query MUST select and alias one "
                + "column per name below, using these exact aliases in addition to the value columns the request "
                + "asks for:");
        for (String column : requiredResultProperties) {
            text.append("\n- ").append(column);
        }
        return text.toString();
    }
}

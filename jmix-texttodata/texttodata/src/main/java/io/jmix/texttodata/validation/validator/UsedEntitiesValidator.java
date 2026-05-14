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

package io.jmix.texttodata.validation.validator;

import io.jmix.core.JmixOrder;
import io.jmix.texttodata.generation.GeneratedJpqlResult;
import io.jmix.texttodata.introspection.registry.DomainModelRegistry;
import io.jmix.texttodata.validation.JpqlResultValidator;
import io.jmix.texttodata.validation.JpqlValidationIssue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("textdt_UsedEntitiesValidator")
public class UsedEntitiesValidator implements JpqlResultValidator, Ordered {

    @Autowired
    protected DomainModelRegistry domainModelRegistry;

    @Override
    public List<JpqlValidationIssue> validate(GeneratedJpqlResult result) {
        List<JpqlValidationIssue> issues = new ArrayList<>();

        for (String usedEntity : result.getUsedEntities()) {
            if (!domainModelRegistry.containsEntity(usedEntity)) {
                issues.add(new JpqlValidationIssue("usedEntity.unknown",
                        "Unknown used entity: " + usedEntity));
            }
        }

        return issues;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 900;
    }
}

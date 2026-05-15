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

package io.jmix.texttodata.dataload.validation.validator;

import io.jmix.core.JmixOrder;
import io.jmix.texttodata.dataload.generation.GeneratedJpqlResult;
import io.jmix.texttodata.dataload.validation.JpqlValidationIssue;
import io.jmix.texttodata.dataload.validation.JpqlResultValidator;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("textdt_JpqlPresenceValidator")
public class JpqlPresenceValidator implements JpqlResultValidator, Ordered {

    @Override
    public List<JpqlValidationIssue> validate(GeneratedJpqlResult result) {
        if (result.getJpql() == null || result.getJpql().isBlank()) {
            return List.of(new JpqlValidationIssue("jpql.blank", "JPQL is blank"));
        }
        return List.of();
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 100;
    }
}

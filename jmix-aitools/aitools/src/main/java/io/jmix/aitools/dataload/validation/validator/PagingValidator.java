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

import io.jmix.core.JmixOrder;
import io.jmix.aitools.dataload.execution.GeneratedJpqlResult;
import io.jmix.aitools.dataload.validation.JpqlValidationIssue;
import io.jmix.aitools.dataload.validation.JpqlResultValidator;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks that the paging hints are sane: {@code maxResults} positive and {@code firstResult}
 * zero or greater.
 */
@Component("aitls_PagingValidator")
public class PagingValidator implements JpqlResultValidator, Ordered {

    @Override
    public List<JpqlValidationIssue> validate(GeneratedJpqlResult result) {
        List<JpqlValidationIssue> issues = new ArrayList<>(2);

        Integer maxResults = result.getMaxResults();
        if (maxResults != null && maxResults <= 0) {
            issues.add(new JpqlValidationIssue("maxResults.invalid",
                    "maxResults must be greater than zero"));
        }

        Integer firstResult = result.getFirstResult();
        if (firstResult != null && firstResult < 0) {
            issues.add(new JpqlValidationIssue("firstResult.invalid",
                    "firstResult must be zero or greater"));
        }

        return issues;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 600;
    }
}

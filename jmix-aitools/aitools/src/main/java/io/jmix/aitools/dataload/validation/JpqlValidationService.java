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

package io.jmix.aitools.dataload.validation;

import io.jmix.aitools.dataload.validation.validator.*;
import io.jmix.aitools.dataload.generation.GeneratedJpqlResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("aitols_JpqlValidationService")
public class JpqlValidationService {

    @Autowired(required = false)
    protected List<JpqlResultValidator> validators = List.of();

    /**
     * Validates {@link GeneratedJpqlResult}.
     * <p>
     * The default order of validators:
     * <ol>
     *     <li>{@link JpqlPresenceValidator}</li>
     *     <li>{@link ReadOnlyQueryValidator}</li>
     *     <li>{@link CommonNonJpqlConstructsValidator}</li>
     *     <li>{@link SupportedJmixTemporalConstructsValidator}</li>
     *     <li>{@link SupportedRelativeDateTimeConstantsValidator}</li>
     *     <li>{@link PagingValidator}</li>
     *     <li>{@link JpqlSyntaxValidator}</li>
     *     <li>{@link RootEntityValidator}</li>
     *     <li>{@link UsedEntitiesValidator}</li>
     *     <li>{@link UsedPropertyPathsValidator}</li>
     *     <li>{@link ParametersValidator}</li>
     * </ol>
     *
     * @param generatedJpqlResult JPQL result from LLM
     * @return validation result
     */
    public JpqlValidationResult validate(GeneratedJpqlResult generatedJpqlResult) {
        List<JpqlValidationIssue> issues = new ArrayList<>();

        if (generatedJpqlResult == null) {
            issues.add(new JpqlValidationIssue("result.missing", "Generated JPQL result is null"));
            return invalid(issues);
        }

        for (JpqlResultValidator validator : validators) {
            issues.addAll(validator.validate(generatedJpqlResult));
        }

        return issues.isEmpty()
                ? new JpqlValidationResult(true, List.of())
                : invalid(issues);
    }

    protected JpqlValidationResult invalid(List<JpqlValidationIssue> issues) {
        return new JpqlValidationResult(false, List.copyOf(issues));
    }
}

/*
 * Copyright 2021 Haulmont.
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

package io.jmix.data.impl.jpql.generator;

import io.jmix.core.common.util.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("data_ConditionGeneratorResolver")
public class ConditionGeneratorResolver {

    @Lazy //Break circular dependencies
    @Autowired
    protected List<ConditionGenerator> conditionGenerators;

    public ConditionGenerator getConditionGenerator(ConditionGenerationContext context) {
        Preconditions.checkNotNullArgument(context);

        for (ConditionGenerator conditionGenerator : conditionGenerators) {
            if (conditionGenerator.supports(context)) {
                return conditionGenerator;
            }
        }

        throw new IllegalStateException(String.format("Can't find condition generator for '%s' condition",
                context.getCondition()));
    }
}

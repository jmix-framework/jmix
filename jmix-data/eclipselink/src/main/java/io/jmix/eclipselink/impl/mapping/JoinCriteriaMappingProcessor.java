/*
 * Copyright 2020 Haulmont.
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

package io.jmix.eclipselink.impl.mapping;

import io.jmix.eclipselink.persistence.JoinExpressionProvider;
import io.jmix.eclipselink.persistence.MappingProcessor;
import io.jmix.eclipselink.persistence.MappingProcessorContext;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Uses all instances of {@link JoinExpressionProvider} beans to create and apply additional join criteria to the mapping.
 */
@Component("eclipselink_JoinCriteriaMappingProcessor")
public class JoinCriteriaMappingProcessor implements MappingProcessor {
    @Autowired
    protected ListableBeanFactory beanFactory;

    @Override
    public void process(MappingProcessorContext context) {
        DatabaseMapping mapping = context.getMapping();

        Expression expression = beanFactory.getBeansOfType(JoinExpressionProvider.class)
                .values().stream()
                .map(provider -> provider.getJoinCriteriaExpression(mapping))
                .filter(Objects::nonNull)
                .reduce(Expression::and).orElse(null);

        //Applying additional join criteria, e.g. for soft delete or etc
        if (mapping.isOneToManyMapping() || mapping.isOneToOneMapping()) {
            //Apply expression to mappings
            if (mapping.isOneToManyMapping()) {
                ((OneToManyMapping) mapping).setAdditionalJoinCriteria(expression);
            } else if (mapping.isOneToOneMapping()) {
                ((OneToOneMapping) mapping).setAdditionalJoinCriteria(expression);
            }
        }
    }
}

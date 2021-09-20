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

import io.jmix.core.JmixOrder;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.querycondition.PropertyConditionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("data_KeyValuePropertyConditionGenerator")
@Order(JmixOrder.LOWEST_PRECEDENCE - 10)
public class KeyValuePropertyConditionGenerator extends PropertyConditionGenerator {

    @Autowired
    public KeyValuePropertyConditionGenerator(MetadataTools metadataTools, Metadata metadata) {
        super(metadataTools, metadata);
    }

    @Override
    public boolean supports(ConditionGenerationContext context) {
        return super.supports(context)
                && context.getValueProperties() != null
                && context.getSelectedExpressions() != null;
    }

    @Override
    public String generateWhere(ConditionGenerationContext context) {
        PropertyCondition propertyCondition = (PropertyCondition) context.getCondition();
        if (propertyCondition == null) {
            return "";
        }

        List<String> valueProperties = context.getValueProperties();
        List<String> selectedExpressions = context.getSelectedExpressions();
        if (valueProperties == null || selectedExpressions == null) {
            return "";
        }

        String entityAlias = propertyCondition.getProperty();
        String property = null;
        if (entityAlias.contains(".")) {
            int indexOfDot = entityAlias.indexOf(".");
            property = entityAlias.substring(indexOfDot + 1);
            entityAlias = entityAlias.substring(0, indexOfDot);
        }

        int index = valueProperties.indexOf(entityAlias);
        if (index >= 0 && index < selectedExpressions.size()) {
            entityAlias = selectedExpressions.get(index);
        }

        if (property != null) {
            return generateWhere(propertyCondition, entityAlias, property);
        } else {
            return generateKeyValueWhere(propertyCondition, entityAlias);
        }
    }

    protected String generateKeyValueWhere(PropertyCondition propertyCondition, String entityAlias) {
        if (PropertyConditionUtils.isUnaryOperation(propertyCondition)) {
            return String.format("%s %s",
                    entityAlias,
                    PropertyConditionUtils.getJpqlOperation(propertyCondition));
        } else if (PropertyConditionUtils.isInIntervalOperation(propertyCondition)) {
            String operation = PropertyConditionUtils.getJpqlOperation(propertyCondition);
            String lastSymbol = operation.contains("@between") ? "," : " ";
            return new StringBuilder(operation)
                    .replace(operation.indexOf("{"), operation.indexOf(lastSymbol), entityAlias)
                    .toString();
        } else {
            return String.format("%s %s :%s",
                    entityAlias,
                    PropertyConditionUtils.getJpqlOperation(propertyCondition),
                    propertyCondition.getParameterName());
        }
    }
}

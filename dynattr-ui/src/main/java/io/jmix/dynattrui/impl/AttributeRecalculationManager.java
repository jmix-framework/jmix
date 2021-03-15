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

package io.jmix.dynattrui.impl;

import com.google.common.base.Strings;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.DynAttrUtils;
import io.jmix.dynattr.DynamicAttributes;
import io.jmix.dynattr.DynamicAttributesState;
import io.jmix.dynattrui.DynAttrUiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.support.StaticScriptSource;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("dynat_AttributeRecalculationManager")
public class AttributeRecalculationManager {

    @Autowired
    protected ScriptEvaluator scriptEvaluator;
    @Autowired
    protected AttributeDependencies attributeDependencies;
    @Autowired
    protected DynAttrUiProperties dynAttrUiProperties;

    /**
     * Performs recalculation for all dependent dynamic attributes. Recalculation is performed hierarchically.
     * <p>
     * Recalculation level limited by
     * {@code cuba.dynamicAttributes.maxRecalculationLevel} application property. If this property is not defined
     * then the default value is used (default value is 10).
     *
     * @param entity    entity with loaded dynamic attributes.
     * @param attribute an attribute from which the recalculation begins. Value for this attribute won't be changed,
     *                  it is assumed that this attribute was updated before
     */
    public void recalculateByAttribute(Object entity, AttributeDefinition attribute) {

        Set<AttributeDefinition> dependentAttributes = attributeDependencies.getDependentAttributes(attribute);

        if (!dependentAttributes.isEmpty()) {
            Set<AttributeDefinition> needToRecalculate = new HashSet<>(dependentAttributes);

            int recalculationLevel = 1;
            while (!needToRecalculate.isEmpty()) {

                if (recalculationLevel > dynAttrUiProperties.getMaxRecalculationLevel()) {
                    throw new IllegalStateException(String.format("Recalculation level has reached the maximum allowable value: %d. " +
                            "Check Dynamic Attributes configuration.", dynAttrUiProperties.getMaxRecalculationLevel()));
                }

                Set<AttributeDefinition> nextLevelAttributes = new HashSet<>();

                for (AttributeDefinition dependentAttribute : needToRecalculate) {
                    String script = dependentAttribute.getConfiguration().getRecalculationScript();

                    if (Strings.isNullOrEmpty(script)) {
                        continue;
                    }

                    String propertyName = DynAttrUtils.getPropertyFromAttributeCode(dependentAttribute.getCode());

                    Object oldValue = EntityValues.getValue(entity, propertyName);
                    Object newValue = evaluateNewValue(entity, script);

                    if (!Objects.equals(oldValue, newValue)) {
                        EntityValues.setValue(entity, propertyName, newValue);
                        nextLevelAttributes.addAll(attributeDependencies.getDependentAttributes(attribute));
                    }
                }

                needToRecalculate = nextLevelAttributes;
                recalculationLevel++;
            }
        }
    }

    protected Object evaluateNewValue(Object entity, String script) {
        Map<String, Object> values = new HashMap<>();
        DynamicAttributesState extraState = EntitySystemAccess.getExtraState(entity, DynamicAttributesState.class);
        if (extraState != null && extraState.getDynamicAttributes() != null) {
            DynamicAttributes dynamicAttributes = extraState.getDynamicAttributes();
            for (String key : dynamicAttributes.getKeys()) {
                values.put(key, dynamicAttributes.getValue(key));
            }
        }

        Map<String, Object> params = new HashMap<>();
        params.put("entity", entity);
        params.put("dynamicAttributes", values);

        return scriptEvaluator.evaluate(new StaticScriptSource(script), params);
    }
}

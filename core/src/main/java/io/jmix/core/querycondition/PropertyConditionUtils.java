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

package io.jmix.core.querycondition;

import com.google.common.base.Strings;
import org.apache.commons.lang3.RandomStringUtils;

public class PropertyConditionUtils {

    /**
     * @param propertyCondition property condition
     * @return true if property condition operation is unary (doesn't require parameter value), e.g "is null"
     */
    public static boolean isUnaryOperation(PropertyCondition propertyCondition) {
        String operation = propertyCondition.getOperation();
        return PropertyCondition.Operation.IS_NULL.equals(operation) ||
                PropertyCondition.Operation.IS_NOT_NULL.equals(operation);
    }

    /**
     * @param property an entity property
     * @return a parameter name
     */
    public static String generateParameterName(String property) {
        return (Strings.nullToEmpty(property)
                + RandomStringUtils.randomAlphabetic(8)).replace(".", "_");
    }

}

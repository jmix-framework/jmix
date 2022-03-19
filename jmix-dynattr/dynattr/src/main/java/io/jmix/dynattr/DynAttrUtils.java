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

package io.jmix.dynattr;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

public final class DynAttrUtils {
    private DynAttrUtils() {
    }

    /**
     * Remove dynamic attribute marker (+) from attribute code (if exists)
     */
    public static boolean isDynamicAttributeProperty(String attributeCode) {
        return attributeCode != null && attributeCode.startsWith("+");
    }

    /**
     * Remove dynamic attribute marker (+) from attribute code (if exists)
     */
    public static String getAttributeCodeFromProperty(String propertyName) {
        return propertyName.startsWith("+") ? propertyName.substring(1) : propertyName;
    }

    /**
     * Add dynamic attribute marker (+) to attribute code (if does not exist)
     */
    public static String getPropertyFromAttributeCode(String attributeCode) {
        return attributeCode.startsWith("+") ? attributeCode : "+" + attributeCode;
    }

    public static Class getDatatypeClass(AttributeType attributeType) {
        switch (attributeType) {
            case STRING:
                return String.class;
            case INTEGER:
                return Integer.class;
            case DOUBLE:
                return Double.class;
            case DECIMAL:
                return BigDecimal.class;
            case BOOLEAN:
                return Boolean.class;
            case DATE:
                return Date.class;
            case DATE_WITHOUT_TIME:
                return LocalDate.class;
            case ENUMERATION:
                return String.class;
        }
        return String.class;
    }
}
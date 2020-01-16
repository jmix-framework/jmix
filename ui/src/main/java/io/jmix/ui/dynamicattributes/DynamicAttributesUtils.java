/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.dynamicattributes;

import io.jmix.core.AppBeans;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;

import javax.annotation.Nullable;
import java.util.UUID;

// todo dummy component to observe the surface of dynamic attributes usage

public final class DynamicAttributesUtils {
    private DynamicAttributesUtils() {
    }

    /**
     * Get special meta property path object for dynamic attribute
     */
    public static MetaPropertyPath getMetaPropertyPath(MetaClass metaClass, CategoryAttribute attribute) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get special meta property path object for dynamic attribute by code
     */
    @Nullable
    public static MetaPropertyPath getMetaPropertyPath(MetaClass metaClass, String attributeCode) {
        return AppBeans.get(DynamicAttributesTools.NAME, DynamicAttributesTools.class)
                .getMetaPropertyPath(metaClass, attributeCode);
    }

    /**
     * Get special meta property path object for dynamic attribute id
     */
    @Nullable
    public static MetaPropertyPath getMetaPropertyPath(MetaClass metaClass, UUID attributeId) {
        return AppBeans.get(DynamicAttributesTools.NAME, DynamicAttributesTools.class)
                .getMetaPropertyPath(metaClass, attributeId);
    }

    /**
     * Remove dynamic attribute marker (+) from attribute code (if exists)
     */
    public static String decodeAttributeCode(String attributeCode) {
        return attributeCode.startsWith("+") ? attributeCode.substring(1) : attributeCode;
    }

    /**
     * Add dynamic attribute marker (+) to attribute code (if does not exist)
     */
    public static String encodeAttributeCode(String attributeCode) {
        return attributeCode.startsWith("+") ? attributeCode : "+" + attributeCode;
    }

    /**
     * Check if the name has dynamic attribute marker
     */
    public static boolean isDynamicAttribute(String name) {
//        return name.startsWith("+");
        return false;
    }

    /**
     * Check if the meta property is dynamic attribute property
     */
    public static boolean isDynamicAttribute(MetaProperty metaProperty) {
//        return metaProperty instanceof DynamicAttributesMetaProperty;
        return false;
    }

    public static CategoryAttribute getCategoryAttribute(MetaProperty metaProperty) {
        return null;
    }

    /**
     * Resolve attribute value's Java class
     */
    public static Class getAttributeClass(CategoryAttribute attribute) {
        throw new UnsupportedOperationException();
    }

    /**
     * For collection dynamic attributes the method returns a list of formatted collection items joined with the comma,
     * for non-collection dynamic attribute a formatted value is returned.
     *
     * @see DynamicAttributesTools#getDynamicAttributeValueAsString(MetaProperty, Object)
     */
    public static String getDynamicAttributeValueAsString(MetaProperty metaProperty, Object value) {
        return AppBeans.get(DynamicAttributesTools.NAME, DynamicAttributesTools.class)
                .getDynamicAttributeValueAsString(metaProperty, value);
    }
}

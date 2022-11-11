/*
 * Copyright 2022 Haulmont.
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

package io.jmix.datatoolsflowui.view.entityinspector;

import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaProperty;

import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.util.UUID;

public final class EntityFormLayoutUtils {

    public static boolean isRequired(MetaProperty metaProperty) {
        if (metaProperty.isMandatory())
            return true;

        ManyToOne many2One = metaProperty.getAnnotatedElement().getAnnotation(ManyToOne.class);
        if (many2One != null && !many2One.optional())
            return true;

        OneToOne one2one = metaProperty.getAnnotatedElement().getAnnotation(OneToOne.class);
        return one2one != null && !one2one.optional();
    }

    public static boolean isByteArray(MetaProperty metaProperty) {
        return metaProperty.getRange().asDatatype().getJavaClass().equals(byte[].class);
    }

    public static boolean isUuid(MetaProperty metaProperty) {
        return metaProperty.getRange().asDatatype().getJavaClass().equals(UUID.class);
    }

    public static boolean isBoolean(MetaProperty metaProperty) {
        return metaProperty.getRange().isDatatype() && metaProperty.getRange().asDatatype().getJavaClass().equals(Boolean.class);
    }

    /**
     * Checks if the property is embedded
     *
     * @param metaProperty meta property
     * @return true if embedded, false otherwise
     */
    public static boolean isEmbedded(MetaProperty metaProperty) {
        return metaProperty.getAnnotatedElement().isAnnotationPresent(javax.persistence.Embedded.class)
                || metaProperty.getAnnotatedElement().isAnnotationPresent(javax.persistence.EmbeddedId.class);
    }

    /**
     * Checks if the property is many
     *
     * @param metaProperty meta property
     * @return true if many, false otherwise
     */
    public static boolean isMany(MetaProperty metaProperty) {
        return metaProperty.getRange().getCardinality().isMany();
    }

    /**
     * @param metaProperty meta property
     * @param item         entity containing property of the given meta property
     * @return true if property require text area component; that is if it either too long or contains line separators
     */
    public static boolean requireTextArea(MetaProperty metaProperty, Object item, int maxTextFieldLength) {
        if (!String.class.equals(metaProperty.getJavaType())) {
            return false;
        }

        Integer textLength = (Integer) metaProperty.getAnnotations().get("length");
        boolean isLong = textLength != null && textLength > maxTextFieldLength;

        Object value = EntityValues.getValue(item, metaProperty.getName());
        boolean isContainsSeparator = value != null && containsSeparator((String) value);

        return isLong || isContainsSeparator;
    }

    public static boolean containsSeparator(String s) {
        return s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0;
    }

}

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

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.UUID;

// todo dummy component to observe the surface of dynamic attributes usage on ui

@Component(DynamicAttributesTools.NAME)
public class DynamicAttributesTools {

    public static final String NAME = "cuba_DynamicAttributesTools";

    /**
     * Get special meta property path object for dynamic attribute by code
     */
    @Nullable
    public MetaPropertyPath getMetaPropertyPath(MetaClass metaClass, String attributeCode) {
        return null;
    }

    /**
     * Get special meta property path object for dynamic attribute id
     */
    @Nullable
    public MetaPropertyPath getMetaPropertyPath(MetaClass metaClass, UUID attributeId) {
        return null;
    }

    /**
     * For collection dynamic attributes the method returns a list of formatted collection items joined with the comma,
     * for non-collection dynamic attribute a formatted value is returned
     */
    @SuppressWarnings("unchecked")
    public String getDynamicAttributeValueAsString(MetaProperty metaProperty, Object value) {
        return null;
    }

}

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

package io.jmix.uidata.importexport;

import io.jmix.core.EntityAttributeImportExtension;
import io.jmix.core.JmixOrder;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.entity.LogicalFilterCondition;
import io.jmix.uidata.entity.FilterConditionConverter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.persistence.Convert;

@Component("ui_FilterConditionAttributeImportExtension")
@Order(JmixOrder.LOWEST_PRECEDENCE)
public class FilterConditionAttributeImportExtension implements EntityAttributeImportExtension {

    @Override
    public boolean supports(MetaProperty property) {
        return property.getRange().isClass()
                && LogicalFilterCondition.class.isAssignableFrom(property.getRange().asClass().getJavaClass())
                && property.getAnnotatedElement().isAnnotationPresent(Convert.class)
                && property.getAnnotatedElement().getAnnotation(Convert.class).converter() == FilterConditionConverter.class;
    }

    @Override
    public void importEntityAttribute(MetaProperty property, Object srcEntity, Object dstEntity) {
        String propertyName = property.getName();
        EntityValues.setValue(dstEntity, propertyName, EntityValues.getValue(srcEntity, propertyName));
    }
}

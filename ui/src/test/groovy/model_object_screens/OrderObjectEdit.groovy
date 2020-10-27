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

package model_object_screens


import io.jmix.core.SaveContext
import io.jmix.ui.component.DateField
import io.jmix.ui.component.EntityPicker
import io.jmix.ui.component.Table
import io.jmix.ui.component.TextField
import io.jmix.ui.model.CollectionPropertyContainer
import io.jmix.ui.screen.*
import org.springframework.beans.factory.annotation.Autowired
import test_support.entity.model_objects.CustomerObject
import test_support.entity.model_objects.OrderLineObject
import test_support.entity.model_objects.OrderObject

import java.time.LocalDate

@UiController("test_OrderObject.edit")
@UiDescriptor("order-object-edit.xml")
@EditedEntityContainer("orderObjectDc")
class OrderObjectEdit extends StandardEditor<OrderObject> {

    @Autowired
    EntityPicker<CustomerObject> customerPicker
    @Autowired
    DateField<LocalDate> dateField
    @Autowired
    TextField<String> numberField
    @Autowired
    CollectionPropertyContainer<OrderLineObject> lineObjectsDc
    @Autowired
    Table<OrderLineObject> linesTable


    @Install(target = Target.DATA_CONTEXT)
    private Set<Object> commitDelegate(SaveContext saveContext) {
        for (Object entity : saveContext.getEntitiesToSave()) {
            TestJmixEntitiesStorage.getInstance().save(entity)
        }
        return new HashSet<>(saveContext.getEntitiesToSave())
    }

}
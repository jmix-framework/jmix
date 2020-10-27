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

import io.jmix.core.LoadContext
import io.jmix.ui.component.GroupTable
import io.jmix.ui.model.CollectionContainer
import io.jmix.ui.screen.*
import org.springframework.beans.factory.annotation.Autowired
import test_support.entity.model_objects.OrderObject

@UiController("test_OrderObject.browse")
@UiDescriptor("order-object-browse.xml")
@LookupComponent("orderObjectsTable")
class OrderObjectBrowse extends StandardLookup<OrderObject> {

    @Autowired
    GroupTable<OrderObject> orderObjectsTable
    @Autowired
    CollectionContainer<OrderObject> orderObjectsDc

    @Install(to = "orderObjectsDl", target = Target.DATA_LOADER)
    private List<OrderObject> orderObjectsDlLoadDelegate(LoadContext<OrderObject> loadContext) {
        return TestJmixEntitiesStorage.getInstance().getAll(OrderObject.class)
    }
}
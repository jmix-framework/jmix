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

package browser_editor_interaction

import io.jmix.ui.component.GroupTable
import io.jmix.ui.model.CollectionContainer
import io.jmix.ui.model.DataContext
import io.jmix.ui.screen.LookupComponent
import io.jmix.ui.screen.StandardLookup
import io.jmix.ui.screen.UiController
import io.jmix.ui.screen.UiDescriptor
import org.springframework.beans.factory.annotation.Autowired
import test_support.entity.sales.Customer

@UiController('test_Customer.browse')
@UiDescriptor('customer-browse-with-readwrite-datacontext.xml')
@LookupComponent('customersTable')
class CustomerBrowseWithReadWriteDataContext extends StandardLookup<Customer> {

    @Autowired
    GroupTable<Customer> customersTable

    @Autowired
    DataContext dataContext

    @Autowired
    CollectionContainer<Customer> customersDc
}
/*
 * Copyright (c) 2008-2017 Haulmont.
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

package spec.haulmont.cuba.core.views

import com.haulmont.cuba.core.model.sales.Order
import io.jmix.core.FetchPlan
import io.jmix.core.FetchPlanRepository
import spec.haulmont.cuba.core.CoreTestSpecification

import org.springframework.beans.factory.annotation.Autowired

class BaseViewTest extends CoreTestSpecification {

    @Autowired
    FetchPlanRepository viewRepository

    def "base view"() {

        def view = viewRepository.getFetchPlan(Order, FetchPlan.BASE)

        expect:

        view.containsProperty('number')
        view.containsProperty('date')
        view.containsProperty('amount')
        view.containsProperty('customer')
        !view.containsProperty('user')
    }
}

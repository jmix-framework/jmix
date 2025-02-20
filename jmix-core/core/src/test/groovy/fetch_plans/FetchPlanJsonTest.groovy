/*
 * Copyright 2024 Haulmont.
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

package fetch_plans

import io.jmix.core.CoreConfiguration
import io.jmix.core.FetchPlan
import io.jmix.core.FetchPlanSerialization
import io.jmix.core.FetchPlans
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.addon1.TestAddon1Configuration
import test_support.app.TestAppConfiguration
import test_support.app.entity.sales.Order

@ContextConfiguration(classes = [CoreConfiguration, TestAddon1Configuration, TestAppConfiguration])
class FetchPlanJsonTest extends Specification {

    @Autowired
    FetchPlans fetchPlans
    @Autowired
    FetchPlanSerialization fetchPlanSerialization

    def "test serialization"() {
        def fetchPlan = fetchPlans.builder(Order)
                .addFetchPlan(FetchPlan.BASE)
                .add('customer', FetchPlan.BASE)
                .build()

        when:
        def json = fetchPlanSerialization.toJson(fetchPlan)

        then:
        FetchPlan fetchPlan1 = fetchPlanSerialization.fromJson('''
        {
          "name" : "",
          "entity" : "core_Order",
          "properties" : [ "date", "amount", "updatedBy", "version", "deletedBy", "deleteTs", "number", "createdBy", "createTs", "id", "updateTs", {
            "name" : "customer",
            "fetchPlan" : {
              "properties" : [ "deleteTs", "updatedBy", "createdBy", "name", "createTs", "id", "version", "updateTs", "deletedBy", "status" ]
            }
          } ]
        }        
        ''')
        fetchPlan1 == fetchPlan
    }

    def "test deserializing without name"() {
        def fetchPlan = fetchPlans.builder(Order)
                .addFetchPlan(FetchPlan.BASE)
                .add('customer', FetchPlan.BASE)
                .build()

        when:
        def json = fetchPlanSerialization.toJson(fetchPlan)

        then:
        FetchPlan fetchPlan1 = fetchPlanSerialization.fromJson('''
        {
          "entity" : "core_Order",
          "properties" : [ "date", "amount", "updatedBy", "version", "deletedBy", "deleteTs", "number", "createdBy", "createTs", "id", "updateTs", {
            "name" : "customer",
            "fetchPlan" : {
              "properties" : [ "deleteTs", "updatedBy", "createdBy", "name", "createTs", "id", "version", "updateTs", "deletedBy", "status" ]
            }
          } ]
        }        
        ''')
        fetchPlan1 == fetchPlan
    }
}

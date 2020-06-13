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

package fetch_plans

import test_support.addon1.TestAddon1Configuration
import test_support.addon1.entity.TestAddon1Entity
import test_support.AppContextTestExecutionListener
import test_support.app.TestAppConfiguration
import test_support.app.entity.Pet
import io.jmix.core.CoreConfiguration
import io.jmix.core.FetchPlan
import io.jmix.core.FetchPlanRepository
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired

@ContextConfiguration(classes = [CoreConfiguration, TestAddon1Configuration, TestAppConfiguration])
@TestExecutionListeners(value = AppContextTestExecutionListener,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class FetchPlanRepositoryTest extends Specification {

    @Autowired
    FetchPlanRepository repository

    def "fetchPlan is deployed from add-on's fetch-plans.xml file"() {
        when:

        def fetchPlan = repository.getFetchPlan(TestAddon1Entity, 'test-fp-1')

        then:

        fetchPlan.containsProperty('name')
    }

    def "predefined fetch plans do not contain system properties"() {

        def localFetchPlan = repository.getFetchPlan(Pet.class, FetchPlan.LOCAL)

        expect:
        !containsSystemProperties(localFetchPlan)

    }

    private boolean containsSystemProperties(FetchPlan fetchPlan) {
        return fetchPlan.containsProperty("id") ||
            fetchPlan.containsProperty("version") ||
            fetchPlan.containsProperty("deleteTs") ||
            fetchPlan.containsProperty("deletedBy") ||
            fetchPlan.containsProperty("createTs") ||
            fetchPlan.containsProperty("createdBy") ||
            fetchPlan.containsProperty("updateTs") ||
            fetchPlan.containsProperty("updatedBy")
    }
}

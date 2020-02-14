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

package views

import test_support.addon1.TestAddon1Configuration
import test_support.addon1.entity.TestAddon1Entity
import test_support.AppContextTestExecutionListener
import test_support.app.TestAppConfiguration
import test_support.app.entity.Pet
import io.jmix.core.JmixCoreConfiguration
import io.jmix.core.FetchPlan
import io.jmix.core.FetchPlanRepository
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import spock.lang.Specification

import javax.inject.Inject

@ContextConfiguration(classes = [JmixCoreConfiguration, TestAddon1Configuration, TestAppConfiguration])
@TestExecutionListeners(value = AppContextTestExecutionListener,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class ViewRepositoryTest extends Specification {

    @Inject
    FetchPlanRepository viewRepository

    def "view is deployed from add-on's view.xml file"() {
        when:

        def view = viewRepository.getFetchPlan(TestAddon1Entity, 'test-view-1')

        then:

        view.containsProperty('name')
    }

    def "predefined views do not contain system properties"() {

        def localView = viewRepository.getFetchPlan(Pet.class, FetchPlan.LOCAL)

        expect:
        !containsSystemProperties(localView)

    }

    private boolean containsSystemProperties(FetchPlan view) {
        return view.containsProperty("id") ||
            view.containsProperty("version") ||
            view.containsProperty("deleteTs") ||
            view.containsProperty("deletedBy") ||
            view.containsProperty("createTs") ||
            view.containsProperty("createdBy") ||
            view.containsProperty("updateTs") ||
            view.containsProperty("updatedBy")
    }
}

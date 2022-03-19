/*
 * Copyright (c) 2020 Haulmont.
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

package fetch_plan


import fetch_plan.screen.UserEditEmbeddedFetchPlanTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.core.Metadata
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.sec.User

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class FetchPlanTest extends ScreenSpecification {

    @Autowired
    Metadata metadata

    @Override
    void setup() {
        exportScreensPackages(["fetch_plan"])
    }

    def "Embedded view initialized in instance container"() {

        given:
        showTestMainScreen()

        when: "show screen"

        def userEditScreen = screens.create(UserEditEmbeddedFetchPlanTestScreen)
        def user = metadata.create(User)
        user.login = 'admin'
        userEditScreen.setEntityToEdit(user)
        userEditScreen.show()
        def fetchPlan = userEditScreen.userDc.getFetchPlan()

        then: "instance container contains embedded fetchPlan"

        fetchPlan != null
        fetchPlan.name == ""
        fetchPlan.getEntityClass() == User

        and: "fetchPlan extends specified fetchPlan"

        fetchPlan.properties.find { it.name == "login" } != null

        when:

        def groupViewProperty = fetchPlan.properties.find { it.name == "group" }

        then: "fetchPlan has inlined fetch plans"

        groupViewProperty != null
        groupViewProperty.fetchPlan.properties.find { it.name == "name" } != null

        when:

        def userRolesViewProperty = fetchPlan.properties.find { it.name == "userRoles" }

        then: "fetchPlan has properties with deployed views"

        userRolesViewProperty != null
        userRolesViewProperty.fetchPlan.name == "user.edit"
        userRolesViewProperty.fetchPlan.properties.find { it.name == "role" } != null
    }
}

/*
 * Copyright (c) 2008-2019 Haulmont.
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

package spec.haulmont.cuba.web.view

import com.haulmont.cuba.core.model.common.User
import io.jmix.core.Metadata
import org.springframework.beans.factory.annotation.Autowired
import spec.haulmont.cuba.web.UiScreenSpec
import spec.haulmont.cuba.web.view.screens.UserEditEmbeddedViewScreen

class ScreenViewTest extends UiScreenSpec {

    @Autowired
    Metadata metadata

    def setup() {
        exportScreensPackages(['spec.haulmont.cuba.web.view.screens'])
    }

    def "Embedded view initialized in instance container"() {

        given:
        showMainScreen()

        when: "show screen"

        def userEditScreen = screens.create(UserEditEmbeddedViewScreen)
        def user = metadata.create(User)
        user.login = 'admin'
        userEditScreen.setEntityToEdit(user)
        userEditScreen.show()
        def view = userEditScreen.userDc.getView()

        then: "instance container contains embedded view"

        view != null
        view.name == ""
        view.getEntityClass() == User

        and: "view extends specified view"

        view.properties.find { it.name == "login" } != null

        and: "view has system properties"

        view.properties.find { it.name == "updateTs" } != null

        when:

        def groupViewProperty = view.properties.find { it.name == "group" }

        then: "view has inlined views"

        groupViewProperty != null
        groupViewProperty.fetchPlan.properties.find { it.name == "name" } != null

        when:

        def userRolesViewProperty = view.properties.find { it.name == "userRoles" }

        then: "view has properties with deployed views"

        userRolesViewProperty != null
        userRolesViewProperty.fetchPlan.name == "user.edit"
        userRolesViewProperty.fetchPlan.properties.find { it.name == "role" } != null
    }
}

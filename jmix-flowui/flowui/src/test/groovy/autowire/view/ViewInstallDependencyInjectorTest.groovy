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

package autowire.view

import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class ViewInstallDependencyInjectorTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("autowire.view")
    }

    def "Autowire #dataElement install points into the view"() {
        when: "InstallDependencyInjectorView is opened"
        def view = navigateToView ViewInstallDependencyInjectorView

        then: "The install point will be set"
        view."${dataElement}"."get${installName}"() != null

        where:
        dataElement << [
                "dataContext", "collectionDl",
                "facet", "component",
                "dataGrid", "dataGrid", "dataGrid"
        ]
        installName << [
                "SaveDelegate", "LoadDelegate",
                "SaveSettingsDelegate", "ItemLabelGenerator",
                "PartNameGenerator", "DropFilter", "DragFilter"
        ]
    }
}

/*
 * Copyright 2026 Haulmont.
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

package view_registry_ext

import io.jmix.flowui.view.ViewRegistry
import view_registry_ext.view.extension.ExtendingView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class ViewExtensionTest extends FlowuiTestSpecification {

    @Autowired
    ViewRegistry viewRegistry

    def "extended view in the same package should be registered"() {
        when:
        registerViewBasePackages("view_registry_ext.view.extension")

        then:
        def viewInfo = viewRegistry.getViewInfo("test_View")
        viewInfo.controllerClass == ExtendingView
    }

    def "unrelated views with same id should throw exception"() {
        when:
        registerViewBasePackages("view_registry_ext.view.conflict")

        then:
        thrown(RuntimeException)
    }
}

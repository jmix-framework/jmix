/*
 * Copyright 2022 Haulmont.
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

package view_attributes

import com.vaadin.flow.server.VaadinSession
import io.jmix.flowui.view.StandardOutcome
import io.jmix.flowui.view.ViewAttributes
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification
import view_attributes.view.ViewAttributesView

@SpringBootTest
class ViewAttributesTest extends FlowuiTestSpecification {

    private static final String STR_VALUE = "stringValue"
    private static final String STR_NAME = "stringName"

    private static final String INT_VALUE = 1
    private static final String INT_NAME = "integerName"

    @Override
    void setup() {
        registerScreenBasePackages("view_attributes.view")
    }

    def "Check saving and removing attributes after View opened and closed"() {
        def view = openScreen(ViewAttributesView)
        def attributes = view.getAttributes()

        when: "Open View and set attribute"

        attributes.setAttribute(STR_NAME, STR_VALUE)

        then: "Vaadin session should contain set attribute for the View"

        def sessionAttrs = getAttributesFromSession(view.getId().get())

        sessionAttrs[0].getAttribute(STR_NAME) == STR_VALUE
        sessionAttrs[0].getAttribute(STR_NAME) == attributes.getAttribute(STR_NAME)

        and: "After closing View attribute should be removed from Vaadin session"
        view.close(StandardOutcome.CLOSE)

        def sessionAttrs1 = getAttributesFromSession(view.getId().get())
        sessionAttrs1 == null
    }

    def "Check removing attribute"() {
        def view = openScreen(ViewAttributesView)
        def attributes = view.getAttributes()

        when: "Add two attributes"

        attributes.setAttribute(STR_NAME, STR_VALUE)
        attributes.setAttribute(INT_NAME, INT_VALUE)

        then: "Attributes should be stored in Vaadin session"

        def sessionAttributes = getAttributesFromSession(view.getId().get())

        attributes.getAttribute(STR_NAME) == STR_VALUE
        attributes.getAttribute(STR_NAME) == sessionAttributes[0].getAttribute(STR_NAME)

        attributes.getAttribute(INT_NAME) == INT_VALUE
        attributes.getAttribute(INT_NAME) == sessionAttributes[0].getAttribute(INT_NAME)

        when: "Remove string attribute"

        attributes.removeAttribute(STR_NAME)

        then: "String attribute should be removed from Vaadin session"

        def sessionAttributes1 = getAttributesFromSession(view.getId().get())

        attributes.getAttribute(STR_NAME) == null
        sessionAttributes1[0].getAttribute(STR_NAME) == null

        when: "Remove last attribute"

        attributes.removeAttribute(INT_NAME)

        then: "Session should not contain attributes for the View at all"

        getAttributesFromSession(view.getId().get()) == null
    }

    private Set<ViewAttributes.Attributes> getAttributesFromSession(String viewId) {
        def session = VaadinSession.getCurrent()
        return (Set<ViewAttributes.Attributes>) session.getAttribute(viewId)
    }
}

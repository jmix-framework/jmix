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

package navigation

import com.google.common.collect.ImmutableMap
import com.vaadin.flow.router.QueryParameters
import io.jmix.flowui.view.navigation.RouteSupport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class RouteSupportTest extends FlowuiTestSpecification {

    @Autowired
    RouteSupport routeSupport

    QueryParameters baseQueryParameters

    @Override
    void setup() {
        // param1=foo&param1=bar&param2=value
        baseQueryParameters = new QueryParameters(
                ImmutableMap.of(
                        "param1", List.of("foo", "bar"),
                        "param2", List.of("value")
                ))
    }

    def "QueryParameters: adding single value"() {
        def param3Name = "param3"

        when: "add a new parameter"
        // param3=value1&param1=foo&param1=bar&param2=value
        def queryParameters = routeSupport.addQueryParameter(baseQueryParameters, param3Name, "value1")

        then: "total size is 3 and the new param has a single value"
        queryParameters.getParameters().size() == 3
        queryParameters.getParameters().get(param3Name).size() == 1

        when: "add the same parameter with the same value"
        // param3=value1&param1=foo&param1=bar&param2=value
        queryParameters = routeSupport.addQueryParameter(queryParameters, param3Name, "value1")

        then: "total size is 3 and the new param has a single value"
        queryParameters.getParameters().size() == 3
        queryParameters.getParameters().get(param3Name).size() == 1

        when: "add the same parameter with a new value"
        // param3=value1&param3=value2&param1=foo&param1=bar&param2=value
        queryParameters = routeSupport.addQueryParameter(queryParameters, param3Name, "value2")

        then: "total size is 3 and the new param has two values"
        queryParameters.getParameters().size() == 3
        queryParameters.getParameters().get(param3Name).size() == 2
    }

    def "QueryParameters: adding multiple values"() {
        def param3Name = "param3"

        when: "add new parameters"
        // param3=value1&param3=value2&param1=foo&param1=bar&param2=value
        def queryParameters =
                routeSupport.addQueryParameter(baseQueryParameters, param3Name, ["value1", "value2"])

        then: "total size is 3 and the new param has two values"
        queryParameters.getParameters().size() == 3
        queryParameters.getParameters().get(param3Name).size() == 2

        when: "add the same parameter with the same values"
        // param3=value1&param3=value2&param1=foo&param1=bar&param2=value
        queryParameters = routeSupport.addQueryParameter(queryParameters, param3Name, ["value1", "value2"])

        then: "total size is 3 and the new param has two values"
        queryParameters.getParameters().size() == 3
        queryParameters.getParameters().get(param3Name).size() == 2

        when: "add the same parameter with a new value and one same value"
        // param3=value1&param3=value2&param3=value3&param1=foo&param1=bar&param2=value
        queryParameters = routeSupport.addQueryParameter(queryParameters, param3Name, ["value2", "value3"])

        then: "total size is 3 and the new param has three values"
        queryParameters.getParameters().size() == 3
        queryParameters.getParameters().get(param3Name).size() == 3
    }

    def "QueryParameters: setting single value"() {
        def param1Name = "param1"
        def param3Name = "param3"

        when: "set a new parameter"
        def value = "value1"
        def queryParameters =
                routeSupport.setQueryParameter(baseQueryParameters, param3Name, value)

        then: "total size is 3 and the new param has a single value"
        queryParameters.getParameters().size() == 3
        queryParameters.getParameters().get(param3Name).size() == 1
        queryParameters.getParameters().get(param3Name).get(0) == value

        when: "set the same parameter with a new value"
        value = "value2"
        queryParameters =
                routeSupport.setQueryParameter(queryParameters, param3Name, value)

        then: "total size is 3 and the new param has a single value"
        queryParameters.getParameters().size() == 3
        queryParameters.getParameters().get(param3Name).size() == 1
        queryParameters.getParameters().get(param3Name).get(0) == value

        when: "set a new value for the existing param"
        queryParameters =
                routeSupport.setQueryParameter(queryParameters, param1Name, value)

        then: "total size is 3 and the existing param has a single value"
        queryParameters.getParameters().size() == 3
        queryParameters.getParameters().get(param1Name).size() == 1
        queryParameters.getParameters().get(param1Name).get(0) == value
    }

    def "QueryParameters: setting multiple values"() {
        def param1Name = "param1"
        def param3Name = "param3"

        when: "set a new parameter"
        def value = ["value1", "value2"]
        def queryParameters =
                routeSupport.setQueryParameter(baseQueryParameters, param3Name, value)

        then: "total size is 3 and the new param has two values"
        queryParameters.getParameters().size() == 3
        queryParameters.getParameters().get(param3Name).size() == 2
        queryParameters.getParameters().get(param3Name) == value

        when: "set the same parameter with new values"
        value = ["value3", "value4"]
        queryParameters =
                routeSupport.setQueryParameter(queryParameters, param3Name, value)

        then: "total size is 3 and the new param has two values"
        queryParameters.getParameters().size() == 3
        queryParameters.getParameters().get(param3Name).size() == 2
        queryParameters.getParameters().get(param3Name) == value

        when: "set new values for the existing param"
        queryParameters =
                routeSupport.setQueryParameter(queryParameters, param1Name, value)

        then: "total size is 3 and the existing param has a single value"
        queryParameters.getParameters().size() == 3
        queryParameters.getParameters().get(param1Name).size() == 2
        queryParameters.getParameters().get(param1Name) == value
    }

    def "QueryParameters: merge parameters"() {
        def param1Name = "param1"
        def value = "value"
        def queryParameters = new QueryParameters(
                ImmutableMap.of(
                        param1Name, List.of(value),
                        "param3", List.of("foo", "bar")
                ))

        when: "merge two QueryParameters objects with intersecting params"
        def mergedQueryParameters =
                routeSupport.mergeQueryParameters(baseQueryParameters, queryParameters)

        then: "total size 3 and param value from the first QueryParameters object is replaced with value from the second"
        mergedQueryParameters.getParameters().size() == 3
        mergedQueryParameters.getParameters().get(param1Name).size() == 1
        mergedQueryParameters.getParameters().get(param1Name).get(0) == value
    }
}

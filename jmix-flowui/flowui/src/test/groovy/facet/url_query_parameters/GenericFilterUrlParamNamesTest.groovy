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

package facet.url_query_parameters

import com.vaadin.flow.router.QueryParameters
import facet.url_query_parameters.view.GenericFilterUrlParamNamesTestView
import io.jmix.flowui.facet.urlqueryparameters.GenericFilterUrlQueryParametersBinder
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

import static facet.url_query_parameters.TestGenericFilterUrlBinders.getBinder
import static facet.url_query_parameters.TestGenericFilterUrlBinders.propertyFilterOn

/**
 * Checks the URL parameter names of {@link GenericFilterUrlQueryParametersBinder} set by the
 * {@code configurationParam} and {@code conditionParam} attributes of the facet's
 * {@code <genericFilter>} element.
 */
@SpringBootTest
class GenericFilterUrlParamNamesTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("facet.url_query_parameters", "io.jmix.flowui.app")
    }

    def "writes the configuration and conditions under the parameter names of the facet"() {
        given:
        def screen = navigateToView(GenericFilterUrlParamNamesTestView)
        def binder = getBinder(screen)

        QueryParameters emitted = null
        binder.addUrlQueryParametersChangeListener({ event -> emitted = event.queryParameters })

        when:
        screen.ownersFilter.currentConfiguration = screen.ownersFilter.getConfiguration("byEmail")

        then:
        emitted != null
        emitted.parameters.keySet() == ["ownersFilter_cfg", "ownersFilter_cnd"] as Set
        emitted.parameters["ownersFilter_cfg"] == ["byEmail"]
        emitted.parameters["ownersFilter_cnd"].size() == 1
    }

    def "restores the configuration and its conditions from the parameter names of the facet"() {
        given:
        def screen = navigateToView(GenericFilterUrlParamNamesTestView)
        def binder = getBinder(screen)

        when:
        binder.updateState(new QueryParameters([
                ownersFilter_cfg: ["byEmail"],
                ownersFilter_cnd: ["property:email_contains_abc"]
        ]))

        then:
        def configuration = screen.ownersFilter.currentConfiguration
        configuration.id == "byEmail"
        propertyFilterOn(configuration, "email").value == "abc"
    }

    def "setConfigurationParam renames the configuration parameter only"() {
        given:
        def binder = getBinder(navigateToView(GenericFilterUrlParamNamesTestView))

        when:
        binder.setConfigurationParam("otherCfg")

        then:
        binder.configurationParam == "ownersFilter_otherCfg"
        binder.conditionParam == "ownersFilter_cnd"
    }
}

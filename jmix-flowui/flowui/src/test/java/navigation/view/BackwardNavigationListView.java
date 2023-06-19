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

package navigation.view;

import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.facet.urlqueryparameters.AbstractUrlQueryParametersBinder;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import test_support.entity.sales.Customer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Route(value = "customers")
@ViewController("test_Customer.list")
@ViewDescriptor("backward-navigation-list-view.xml")
@LookupComponent("customersTable")
public class BackwardNavigationListView extends StandardListView<Customer> {

    @ViewComponent
    public JmixButton createBtn;

    @ViewComponent
    public UrlQueryParametersFacet urlQueryParameters;

    public String paramValue;

    @Subscribe
    public void onInit(final InitEvent event) {
        // todo rp rework after https://github.com/jmix-framework/jmix/issues/1843
        urlQueryParameters.registerBinder(new AbstractUrlQueryParametersBinder() {
            @Override
            public void updateState(QueryParameters queryParameters) {
                Map<String, List<String>> parameters = queryParameters.getParameters();

                List<String> values = parameters.getOrDefault("param", Collections.emptyList());
                if (!values.isEmpty()) {
                    paramValue = values.get(0);
                }
            }
        });
    }
}

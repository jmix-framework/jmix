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

package component.genericfilter.view;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.view.*;

/**
 * Has a {@code DataLoadCoordinator}. Two configurations on {@code number}; the first ("match",
 * value FLT_MATCH) is activated via the builder's {@code makeCurrent()} during {@code onInit}.
 * Used to verify that the data actually loaded on open is filtered by the active configuration.
 */
@Route(value = "gf-dlc-filtered-load-test-view")
@ViewController("GfDlcFilteredLoadTestView")
@ViewDescriptor("gf-activation-dlc-view.xml")
public class GfDlcFilteredLoadTestView extends StandardView {

    @ViewComponent
    public GenericFilter genericFilter;

    @Subscribe
    public void onInit(final InitEvent event) {
        PropertyFilter<String> match = genericFilter.filterComponentBuilder()
                .<String>propertyFilter()
                .property("number")
                .operation(PropertyFilter.Operation.EQUAL)
                .build();
        genericFilter.runtimeConfigurationBuilder()
                .id("match")
                .name("Match")
                .add(match, "FLT_MATCH")
                .makeCurrent()
                .buildAndRegister();

        PropertyFilter<String> other = genericFilter.filterComponentBuilder()
                .<String>propertyFilter()
                .property("number")
                .operation(PropertyFilter.Operation.EQUAL)
                .build();
        genericFilter.runtimeConfigurationBuilder()
                .id("other")
                .name("Other")
                .add(other, "FLT_OTHER")
                .buildAndRegister();
    }
}

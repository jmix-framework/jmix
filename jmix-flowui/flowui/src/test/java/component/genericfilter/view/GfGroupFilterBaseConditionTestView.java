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
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.flowui.component.logicalfilter.GroupFilter;
import io.jmix.flowui.view.*;

/**
 * A standalone {@code GroupFilter} with a base condition set on its data loader in {@code onInit}.
 */
@Route(value = "gf-group-filter-base-condition-view")
@ViewController("GfGroupFilterBaseConditionTestView")
@ViewDescriptor("gf-group-filter-base-condition-view.xml")
public class GfGroupFilterBaseConditionTestView extends StandardView {

    @ViewComponent
    public GroupFilter groupFilter;

    @Subscribe
    public void onInit(final InitEvent event) {
        groupFilter.getDataLoader().setCondition(PropertyCondition.greater("amount", 0));
    }
}

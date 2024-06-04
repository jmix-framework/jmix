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

package date_interval.view;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.flowuidata.dateinterval.model.RelativeDateInterval;
import test_support.entity.Project;
import test_support.view.TestMainView;

import java.util.List;

@Route(value = "RelativeDateIntervalTestView", layout = TestMainView.class)
@ViewController
@ViewDescriptor("relative-date-interval-test-view.xml")
public class RelativeDateIntervalTestView extends StandardView {

    @ViewComponent
    public CollectionContainer<Project> projectsDc;
    @ViewComponent
    public PropertyFilter<? super RelativeDateInterval> dateFilter;

    public List<Project> getItems() {
        return projectsDc.getItems();
    }
}

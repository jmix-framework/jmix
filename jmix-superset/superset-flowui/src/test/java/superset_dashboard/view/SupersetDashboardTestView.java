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

package superset_dashboard.view;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;
import io.jmix.supersetflowui.component.SupersetDashboard;
import io.jmix.supersetflowui.component.dataconstraint.DatasetConstraint;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.TestDatasetConstraintsProvider;

import java.util.List;
import java.util.function.Consumer;

@Route("SupersetDashboardTestView")
@ViewController("SupersetDashboardTestView")
@ViewDescriptor("superset-dashboard-test-view.xml")
public class SupersetDashboardTestView extends StandardView {

    @ViewComponent
    public SupersetDashboard dashboard1;
    @ViewComponent
    public SupersetDashboard dashboard2;

    @Autowired
    public TestDatasetConstraintsProvider datasetConstraintsProvider;

    @Install(to = "dashboard1", subject = "datasetConstraintsProvider")
    public List<DatasetConstraint> datasetConstraintsProviderInstall() {
        return datasetConstraintsProvider.getConstraints();
    }
}

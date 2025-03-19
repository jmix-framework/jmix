/*
 * Copyright 2025 Haulmont.
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

package datagrid_settings.view;

import com.vaadin.flow.router.Route;
import io.jmix.core.Metadata;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.Project;
import test_support.entity.User;
import test_support.view.TestMainView;

import java.util.List;

@Route(value = "DataGridSettingsNestedContainerTestVIew", layout = TestMainView.class)
@ViewController
@ViewDescriptor("datagrid-settings-nested-container-test-view.xml")
public class DataGridSettingsNestedContainerTestVIew extends StandardView {

    @Autowired
    private Metadata metadata;

    @ViewComponent
    public DataGrid<User> nestedUsersDataGrid;

    @ViewComponent
    public InstanceContainer<Project> projectDc;

    @Subscribe
    public void onInit(final InitEvent event) {
        Project project = metadata.create(Project.class);

        User user1 = metadata.create(User.class);
        user1.setIssuesCount(1);
        user1.setProject(project);

        User user2 = metadata.create(User.class);
        user2.setIssuesCount(2);
        user2.setProject(project);

        project.setUsers(List.of(user1, user2));

        projectDc.setItem(project);
    }
}

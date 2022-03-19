/*
 * Copyright 2020 Haulmont.
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

package uitest.grouptable_settings.screen;

import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.Table;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import io.jmix.ui.settings.facet.ScreenSettingsFacet;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.Project;

@UiController
@UiDescriptor("group-table-settings-test-screen.xml")
public class GroupTableSettingsTestScreen extends Screen {

    @Autowired
    public ScreenSettingsFacet facet;

    @Autowired
    public GroupTable<Project> projectsTable;

    public Object getColumnId(String id) {
        return projectsTable.getColumn(id).getId();
    }

    public Table.Column<Project> getColumn(String id) {
        return projectsTable.getColumn(id);
    }
}

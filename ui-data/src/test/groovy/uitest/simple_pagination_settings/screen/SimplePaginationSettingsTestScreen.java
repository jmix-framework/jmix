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

package uitest.simple_pagination_settings.screen;

import io.jmix.ui.component.SimplePagination;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import io.jmix.ui.settings.facet.ScreenSettingsFacet;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.Project;

@UiController
@UiDescriptor("simplepagination-settings-test-screen.xml")
public class SimplePaginationSettingsTestScreen extends Screen {

    @Autowired
    public CollectionContainer<Project> projectsDc;

    @Autowired
    public CollectionContainer<Project> projectsTableDc;

    @Autowired
    public CollectionContainer<Project> projectsDataGridDc;

    @Autowired
    public SimplePagination dataGridSimplePagination;

    @Autowired
    public SimplePagination tableSimplePagination;

    @Autowired
    public SimplePagination simplePagination;

    @Autowired
    public ScreenSettingsFacet facet;
}

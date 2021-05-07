/*
 * Copyright 2021 Haulmont.
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

package uitest.presentations.screen;

import io.jmix.ui.component.GroupTable;
import io.jmix.ui.presentation.facet.PresentationsFacet;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.Project;

@UiController
@UiDescriptor("presentations-test-screen.xml")
public class PresentationsTestScreen extends Screen {

    @Autowired
    public PresentationsFacet componentsPresentations;

    @Autowired
    public GroupTable<Project> groupTable;
}

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

package dependency_injector.view;

import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;


@Route("dependency-injector-view")
@ViewController("DependencyInjectorView")
@ViewDescriptor("dependency-injector-view.xml")
public class DependencyInjectorView extends StandardView {

    @ViewComponent("tabSheet")
    private JmixTabSheet tabSheet;

    @ViewComponent("tabSheet.tab1")
    private Tab tabSheetTab1;

    @ViewComponent("tabSheet.tab2")
    private Tab tabSheetTab2;

    public JmixTabSheet getTabSheet() {
        return tabSheet;
    }

    public Tab getTabSheetTab1() {
        return tabSheetTab1;
    }

    public Tab getTabSheetTab2() {
        return tabSheetTab2;
    }
}

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

package component.tabsheet.view;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route("TabSheetTestView")
@ViewController("TabSheetTestView")
@ViewDescriptor("tab-sheet-test-view.xml")
public class TabSheetTestView extends StandardView {

    @ViewComponent
    private JmixTabSheet tabSheet;

    @ViewComponent
    private Tab tab2;

    @ViewComponent
    private Tab tab4;

    @ViewComponent
    private Span tab1Span;

    @ViewComponent
    private JmixButton tab2Button;

    @ViewComponent
    private JmixCheckbox tab3Checkbox;

    public JmixTabSheet getTabSheet() {
        return tabSheet;
    }

    public Tab getTab2() {
        return tab2;
    }

    public Tab getTab4() {
        return tab4;
    }

    public Span getTab1Span() {
        return tab1Span;
    }

    public JmixButton getTab2Button() {
        return tab2Button;
    }

    public JmixCheckbox getTab3Checkbox() {
        return tab3Checkbox;
    }
}
